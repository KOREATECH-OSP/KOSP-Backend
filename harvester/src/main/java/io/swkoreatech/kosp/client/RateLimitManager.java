package io.swkoreatech.kosp.client;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.user.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitManager {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;

    public Mono<Void> waitIfNeeded(Long userId, int threshold) {
        return Mono.defer(() -> {
            User user = userRepository.getById(userId);
            GithubUser githubUser = user.getGithubUser();

            if (githubUser == null) {
                log.warn("User {} has no GitHub account linked", userId);
                return Mono.empty();
            }

            if (githubUser.isRateLimitExpired()) {
                return Mono.empty();
            }

            Instant resetTime = githubUser.getRateLimitResetAt();
            Duration waitTime = Duration.between(Instant.now(), resetTime);

            if (waitTime.isNegative()) {
                return Mono.empty();
            }

            log.warn("Rate limit reached for user {}. Waiting until {}", userId, resetTime);
            return Mono.error(new RateLimitException(
                "Rate limit reached. Reset at: " + resetTime,
                waitTime
            ));
        });
    }

    public Mono<Void> handleRateLimitExceeded(Long userId) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null || githubUser.getRateLimitResetAt() == null) {
            log.error("Rate limit exceeded but no reset time found for user {}", userId);
            return Mono.delay(Duration.ofMinutes(5)).then();
        }

        Instant resetTime = githubUser.getRateLimitResetAt();
        Duration waitTime = Duration.between(Instant.now(), resetTime);

        if (waitTime.isNegative()) {
            return Mono.empty();
        }

        log.error("Rate limit exceeded for user {}! Waiting until: {}", userId, resetTime);
        return Mono.delay(waitTime).then();
    }

    public void updateRateLimitFromHeaders(Long userId, long resetTime) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null) {
            log.warn("User {} has no GitHub account linked", userId);
            return;
        }

        Instant resetAt = Instant.ofEpochMilli(resetTime);
        githubUser.updateRateLimitResetTime(resetAt);
        githubUserRepository.save(githubUser);

        log.debug("Rate limit updated for user {}: reset={}", userId, resetAt);
    }

    public Instant getResetTime(Long userId) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null || githubUser.getRateLimitResetAt() == null) {
            return Instant.now();
        }

        return githubUser.getRateLimitResetAt();
    }
}
