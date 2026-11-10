package io.swkoreatech.kosp.collection;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.client.GithubRestApiClient;
import io.swkoreatech.kosp.client.dto.GithubUserEventResponse;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GitHub Events API를 통해 사용자의 최근 활동 여부를 판단하는 컴포넌트.
 *
 * <p>전체 수집 전에 1회 경량 API 호출로 활동 여부를 확인하여
 * 비활성 사용자에 대한 불필요한 수집을 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityChecker {

    private final GithubRestApiClient restApiClient;
    private final UserRepository userRepository;

    /**
     * lastCrawling 이후 GitHub 활동이 있는지 확인한다.
     *
     * <p>lastCrawling이 null이면 첫 수집으로 간주하여 항상 true를 반환한다.
     *
     * @param userId 사용자 ID
     * @return 활동이 있으면 true, 없으면 false
     */
    public boolean hasActivitySince(Long userId) {
        User user = userRepository.getById(userId);
        GithubUser githubUser = user.getGithubUser();

        if (githubUser == null) {
            log.warn("No GitHub account linked for user {}, skipping activity check", userId);
            return false;
        }

        LocalDateTime since = githubUser.getLastCrawling();
        if (since == null) {
            return true;
        }

        try {
            GithubUserEventResponse[] events = restApiClient
                .getUserEvents(userId, githubUser.getGithubLogin(), githubUser.getGithubToken(), 1)
                .block();

            if (events == null || events.length == 0) {
                return false;
            }

            return hasEventAfter(events, since);
        } catch (Exception e) {
            // 활동 체크 실패 시 수집은 진행
            log.warn("Activity check failed for user {}, proceeding with collection", userId, e);
            return true;
        }
    }

    private boolean hasEventAfter(GithubUserEventResponse[] events, LocalDateTime since) {
        for (GithubUserEventResponse event : events) {
            if (event.getCreatedAt() == null) {
                continue;
            }
            try {
                LocalDateTime eventTime = OffsetDateTime.parse(event.getCreatedAt())
                    .toLocalDateTime();
                if (eventTime.isAfter(since)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to parse event createdAt: {}", event.getCreatedAt());
            }
        }
        return false;
    }
}
