package kr.ac.koreatech.sw.kosp.domain.github.client.rest;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
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
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
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
}
