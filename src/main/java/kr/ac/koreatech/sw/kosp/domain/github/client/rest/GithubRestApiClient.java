package kr.ac.koreatech.sw.kosp.domain.github.client.rest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class GithubRestApiClient {

    private final WebClient webClient;
    private final RateLimitManager rateLimitManager;

    public GithubRestApiClient(
        @Value("${github.api.base-url}") String baseUrl,
        RateLimitManager rateLimitManager
    ) {
        // Configure connection pool to prevent connection exhaustion and reuse connections
        ConnectionProvider connectionProvider = ConnectionProvider.builder("github-pool")
            .maxConnections(60)  // Maximum concurrent connections
            .pendingAcquireMaxCount(500)  // Queue size for waiting requests
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .maxIdleTime(Duration.ofSeconds(30))  // Keep connections alive
            .maxLifeTime(Duration.ofMinutes(5))   // Max connection lifetime
            .evictInBackground(Duration.ofSeconds(120))  // Cleanup interval
            .build();
        
        // Configure HttpClient with connection pool and extended timeouts
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000) // 60 seconds
            .responseTimeout(Duration.ofMinutes(5)) // 5 minutes for large repos
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.MINUTES))
                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.MINUTES)));
        
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")  // Keep connections alive
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build())
            .build();
        this.rateLimitManager = rateLimitManager;
    }

    public <T> Mono<T> get(String uri, String token, Class<T> responseType) {
        return rateLimitManager.waitIfNeeded()
            .then(webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    status -> status.equals(HttpStatus.FORBIDDEN),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.warn("Rate limit exceeded. Waiting...");
                            return rateLimitManager.handleRateLimitExceeded()
                                .then(Mono.error(new RuntimeException("Rate limit exceeded")));
                        })
                )
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(2))
                    .maxBackoff(Duration.ofSeconds(30))
                    .filter(throwable -> 
                        throwable instanceof WebClientResponseException.TooManyRequests ||
                        (throwable.getMessage() != null && (
                            throwable.getMessage().contains("prematurely closed") ||
                            throwable.getMessage().contains("Connection reset") ||
                            throwable.getMessage().contains("Connection refused")
                        ))
                    ))
                .doOnSuccess(response -> {
                    rateLimitManager.recordRequest();
                    log.debug("GET {} - Success", uri);
                })
                .doOnError(error -> log.error("GET {} - Error: {}", uri, error.getMessage()))
            );
    }

    public <T> Mono<T> post(String uri, String token, Object body, Class<T> responseType) {
        return rateLimitManager.waitIfNeeded()
            .then(webClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> {
                    rateLimitManager.recordRequest();
                    log.debug("POST {} - Success", uri);
                })
                .doOnError(error -> log.error("POST {} - Error: {}", uri, error.getMessage()))
            );
    }

    /**
     * Pagination을 지원하여 모든 데이터를 수집합니다.
     */
    public <T> Mono<List<T>> getAllWithPagination(
        String uri,
        String token,
        Class<T> itemType
    ) {
        return Mono.defer(() -> {
            List<T> allItems = new ArrayList<>();
            return collectAllPages(uri, token, itemType, 1, allItems)
                .thenReturn(allItems);
        });
    }

    private <T> Mono<Void> collectAllPages(
        String uri,
        String token,
        Class<T> itemType,
        int page,
        List<T> accumulator
    ) {
        String paginatedUri = buildPaginatedUri(uri, page);
        
        return get(paginatedUri, token, List.class)
            .flatMap(items -> {
                if (items == null || items.isEmpty()) {
                    return Mono.empty();
                }
                
                accumulator.addAll((List<T>) items);
                
                if (items.size() < 100) {
                    return Mono.empty();
                }
                
                return collectAllPages(uri, token, itemType, page + 1, accumulator);
            })
            .then();
    }

    /**
     * 특정 시점 이후의 데이터를 Pagination으로 수집합니다.
     */
    public <T> Mono<List<T>> getAllSince(
        String uri,
        String token,
        LocalDateTime since,
        Class<T> itemType
    ) {
        String sinceParam = since.format(DateTimeFormatter.ISO_DATE_TIME);
        String uriWithSince = buildUriWithParam(uri, "since", sinceParam);
        return getAllWithPagination(uriWithSince, token, itemType);
    }

    private String buildPaginatedUri(String uri, int page) {
        String separator = uri.contains("?") ? "&" : "?";
        return String.format("%s%spage=%d&per_page=100", uri, separator, page);
    }

    private String buildUriWithParam(String uri, String paramName, String paramValue) {
        String separator = uri.contains("?") ? "&" : "?";
        return String.format("%s%s%s=%s", uri, separator, paramName, paramValue);
    }
}
