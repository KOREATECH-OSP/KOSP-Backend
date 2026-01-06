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
        // Configure HttpClient with extended timeouts for large repositories
        HttpClient httpClient = HttpClient.create()
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
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
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
