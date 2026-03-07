package io.swkoreatech.kosp.client;

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
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

/**
 * GitHub REST API 클라이언트.
 *
 * <p>WebClient를 사용하여 GitHub REST API에 요청을 보내며,
 * Rate Limit 관리, 자동 재시도, 페이지네이션 기능을 제공한다.
 * 커넥션 풀링과 타임아웃 설정으로 안정적인 통신을 보장한다.
 */
@Slf4j
@Component
public class GithubRestApiClient {

    private final WebClient webClient;
    private final RateLimitManager rateLimitManager;

    /**
     * GithubRestApiClient를 생성하고 커넥션 풀, 타임아웃 등을 설정한다.
     *
     * @param baseUrl          GitHub REST API 기본 URL
     * @param rateLimitManager Rate Limit 관리자
     */
    public GithubRestApiClient(
        @Value("${github.api.base-url}") String baseUrl,
        RateLimitManager rateLimitManager
    ) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("github-pool")
            .maxConnections(60)
            .pendingAcquireMaxCount(500)
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
            .responseTimeout(Duration.ofMinutes(5))
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.MINUTES));
                conn.addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.MINUTES));
                if (conn.channel().pipeline().get(SslHandler.class) != null) {
                    conn.channel().pipeline().get(SslHandler.class).setHandshakeTimeoutMillis(60000);
                }
            });

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024))
                .build())
            .build();
        this.rateLimitManager = rateLimitManager;
    }

    /**
     * Rate Limit을 확인한 후 GET 요청을 보낸다.
     *
     * @param <T>          응답 타입
     * @param userId       사용자 ID
     * @param uri          요청 URI
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 응답 결과를 담은 Mono
     */
    public <T> Mono<T> get(Long userId, String uri, String token, Class<T> responseType) {
        return rateLimitManager.waitIfNeeded(userId, 100)
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
                            return rateLimitManager.handleRateLimitExceeded(userId)
                                .then(Mono.error(new RuntimeException("Rate limit exceeded")));
                        })
                )
                .toEntity(responseType)
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
                .doOnSuccess(entity -> {
                    if (entity != null) {
                        HttpHeaders headers = entity.getHeaders();
                        String remaining = headers.getFirst("X-RateLimit-Remaining");
                        String reset = headers.getFirst("X-RateLimit-Reset");

                        if (remaining != null && reset != null) {
                            try {
                                long resetLong = Long.parseLong(reset) * 1000;
                                int remainingCount = Integer.parseInt(remaining);
                                rateLimitManager.updateRateLimitFromHeaders(userId, resetLong, remainingCount);
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse rate limit headers: remaining={}, reset={}",
                                    remaining, reset);
                                rateLimitManager.updateRateLimitFromHeaders(userId, 0, 5000);
                            }
                        }
                    }
                    log.debug("GET {} - Success", uri);
                })
                .doOnError(error -> log.error("GET {} - Error: {}", uri, error.getMessage()))
                .map(this::extractBody)
            );
    }

    private <T> T extractBody(org.springframework.http.ResponseEntity<T> entity) {
        if (entity == null) {
            return null;
        }
        return entity.getBody();
    }

    /**
     * Rate Limit을 확인한 후 POST 요청을 보낸다.
     *
     * @param <T>          응답 타입
     * @param userId       사용자 ID
     * @param uri          요청 URI
     * @param token        GitHub 인증 토큰
     * @param body         요청 바디
     * @param responseType 응답 클래스 타입
     * @return 응답 결과를 담은 Mono
     */
    public <T> Mono<T> post(Long userId, String uri, String token, Object body, Class<T> responseType) {
        return rateLimitManager.waitIfNeeded(userId, 100)
            .then(webClient.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> {
                    log.debug("POST {} - Success", uri);
                })
                .doOnError(error -> log.error("POST {} - Error: {}", uri, error.getMessage()))
            );
    }

    /**
     * Rate Limit 사전 확인 없이 GET 요청을 보낸다.
     *
     * <p>Rate Limit 확인을 우회하되, 403 응답 시에는 대기 처리를 수행한다.
     *
     * @param <T>          응답 타입
     * @param userId       사용자 ID
     * @param uri          요청 URI
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 응답 결과를 담은 Mono
     */
    public <T> Mono<T> getBypassingRateLimit(Long userId, String uri, String token, Class<T> responseType) {
        return webClient.get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                status -> status.equals(HttpStatus.FORBIDDEN),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.warn("Rate limit exceeded (Bypass mode). Waiting...");
                        return rateLimitManager.handleRateLimitExceeded(userId)
                            .then(Mono.error(new RuntimeException("Rate limit exceeded")));
                    })
            )
            .toEntity(responseType)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
            .doOnSuccess(entity -> {
                if (entity != null) {
                    HttpHeaders headers = entity.getHeaders();
                    String remaining = headers.getFirst("X-RateLimit-Remaining");
                    String reset = headers.getFirst("X-RateLimit-Reset");

                    if (remaining != null && reset != null) {
                        try {
                            long resetLong = Long.parseLong(reset) * 1000;
                            int remainingCount = Integer.parseInt(remaining);
                            rateLimitManager.updateRateLimitFromHeaders(userId, resetLong, remainingCount);
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse rate limit headers", e);
                            rateLimitManager.updateRateLimitFromHeaders(userId, 0, 5000);
                        }
                    }
                }
            })
            .map(this::extractBody);
    }

    /**
     * 모든 페이지를 순회하여 전체 항목 목록을 조회한다.
     *
     * @param <T>      항목 타입
     * @param userId   사용자 ID
     * @param uri      요청 URI
     * @param token    GitHub 인증 토큰
     * @param itemType 항목 클래스 타입
     * @return 전체 항목 목록을 담은 Mono
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<List<T>> getAllWithPagination(
        Long userId,
        String uri,
        String token,
        Class<T> itemType
    ) {
        return Mono.defer(() -> {
            List<T> allItems = new ArrayList<>();
            return collectAllPages(userId, uri, token, itemType, 1, allItems)
                .thenReturn(allItems);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Mono<Void> collectAllPages(
        Long userId,
        String uri,
        String token,
        Class<T> itemType,
        int page,
        List<T> accumulator
    ) {
        String paginatedUri = buildPaginatedUri(uri, page);

        return get(userId, paginatedUri, token, List.class)
            .flatMap(items -> {
                if (items == null || items.isEmpty()) {
                    return Mono.empty();
                }

                accumulator.addAll((List<T>)items);

                if (items.size() < 100) {
                    return Mono.empty();
                }

                return collectAllPages(userId, uri, token, itemType, page + 1, accumulator);
            })
            .then();
    }

    /**
     * 지정된 시각 이후의 모든 항목을 페이지네이션하여 조회한다.
     *
     * @param <T>      항목 타입
     * @param userId   사용자 ID
     * @param uri      요청 URI
     * @param token    GitHub 인증 토큰
     * @param since    조회 시작 시각
     * @param itemType 항목 클래스 타입
     * @return 조건에 맞는 전체 항목 목록을 담은 Mono
     */
    public <T> Mono<List<T>> getAllSince(
        Long userId,
        String uri,
        String token,
        LocalDateTime since,
        Class<T> itemType
    ) {
        String sinceParam = since.format(DateTimeFormatter.ISO_DATE_TIME);
        String uriWithSince = buildUriWithParam(uri, "since", sinceParam);
        return getAllWithPagination(userId, uriWithSince, token, itemType);
    }

    private String buildPaginatedUri(String uri, int page) {
        String separator = getSeparator(uri);
        return String.format("%s%spage=%d&per_page=100", uri, separator, page);
    }

    private String buildUriWithParam(String uri, String paramName, String paramValue) {
        String separator = getSeparator(uri);
        return String.format("%s%s%s=%s", uri, separator, paramName, paramValue);
    }

    private String getSeparator(String uri) {
        if (uri.contains("?")) {
            return "&";
        }
        return "?";
    }
}
