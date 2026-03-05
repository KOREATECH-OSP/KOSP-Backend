package io.swkoreatech.kosp.client;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.user.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * GitHub API Rate Limit을 관리하는 컴포넌트.
 *
 * <p>사용자별 Rate Limit 잔여량을 확인하고, 임계값 이하일 경우
 * {@link RateLimitException}을 발생시킨다. API 응답 헤더에서
 * Rate Limit 정보를 추출하여 DB에 갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitManager {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;

    /**
     * Rate Limit 잔여량이 임계값 이하인 경우 대기 또는 에러를 발생시킨다.
     *
     * @param userId    사용자 ID
     * @param threshold Rate Limit 임계값
     * @return 대기 완료 시 빈 Mono, 임계값 초과 시 에러 Mono
     */
    public Mono<Void> waitIfNeeded(Long userId, int threshold) {
        return Mono.defer(() -> {
            User user = userRepository.getById(userId);
            GithubUser githubUser = user.getGithubUser();

            if (githubUser == null) {
                log.warn("User {} has no GitHub account linked", userId);
                return Mono.empty();
            }

            int remaining = githubUser.getRemainingOrDefault();
            if (remaining <= threshold) {
                Instant resetTime = githubUser.getRateLimitResetAt();

                if (resetTime == null) {
                    return Mono.empty();
                }

                Duration waitTime = Duration.between(Instant.now(), resetTime);

                log.warn("Rate limit threshold reached for user {}. Remaining: {}, Threshold: {}",
                    userId, remaining, threshold);
                return Mono.error(new RateLimitException(
                    "Rate limit threshold reached. Reset at: " + resetTime,
                    waitTime
                ));
            }

            return Mono.empty();
        });
    }

    /**
     * Rate Limit 초과 시 리셋 시각까지 대기하는 Mono를 반환한다.
     *
     * @param userId 사용자 ID
     * @return 리셋 시각까지 대기하는 Mono
     */
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

    /**
     * API 응답 헤더에서 추출한 Rate Limit 정보로 DB를 갱신한다.
     *
     * @param userId    사용자 ID
     * @param resetTime Rate Limit 리셋 시각 (에포크 밀리초)
     * @param remaining 남은 요청 횟수
     */
    public void updateRateLimitFromHeaders(Long userId, long resetTime, int remaining) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null) {
            log.warn("User {} has no GitHub account linked", userId);
            return;
        }

        Instant resetAt = Instant.ofEpochMilli(resetTime);
        githubUser.updateRateLimit(resetAt, remaining);
        githubUserRepository.save(githubUser);

        log.debug("Rate limit updated for user {}: reset={}, remaining={}", userId, resetAt, remaining);
    }

    /**
     * 사용자의 Rate Limit 리셋 시각을 반환한다.
     *
     * @param userId 사용자 ID
     * @return Rate Limit 리셋 시각, 정보 없으면 현재 시각
     */
    public Instant getResetTime(Long userId) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null || githubUser.getRateLimitResetAt() == null) {
            return Instant.now();
        }

        return githubUser.getRateLimitResetAt();
    }
}
