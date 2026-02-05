package io.swkoreatech.kosp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.queue.Priority;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.trigger.JobSchedulerInitializer;
import io.swkoreatech.kosp.trigger.UserIdProvider;
import io.swkoreatech.kosp.user.GithubUserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rate Limit Persistence and Auto-Initialization Integration Test")
class RateLimitPersistenceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubUserRepository githubUserRepository;

    @Mock
    private JobQueueService jobQueueService;

    @Mock
    private UserIdProvider userIdProvider;

    @InjectMocks
    private RateLimitManager rateLimitManager;

    @InjectMocks
    private JobSchedulerInitializer jobSchedulerInitializer;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("Rate Limit DB 저장 및 조회 테스트")
    class RateLimitPersistenceTest {

        @Test
        @DisplayName("GitHub API 응답 후 resetTime이 DB에 저장되어야 함")
        void shouldPersistResetTimeToDatabase() {
            User user = createUserWithGithubAccount(1001L);
            long resetTimeMillis = Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli();
            
            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.updateRateLimitFromHeaders(user.getId(), resetTimeMillis);

            GithubUser githubUser = user.getGithubUser();
            assertThat(githubUser.getRateLimitResetAt()).isNotNull();
            assertThat(githubUser.getRateLimitResetAt().toEpochMilli()).isEqualTo(resetTimeMillis);
            verify(githubUserRepository, times(1)).save(githubUser);
        }

        @Test
        @DisplayName("DB에 저장된 resetTime을 getResetTime()으로 조회 가능해야 함")
        void shouldRetrieveResetTimeFromDatabase() {
            User user = createUserWithGithubAccount(1002L);
            Instant expectedResetTime = Instant.now().plus(30, ChronoUnit.MINUTES);

            GithubUser githubUser = user.getGithubUser();
            githubUser.updateRateLimitResetTime(expectedResetTime);
            
            when(userRepository.getById(user.getId())).thenReturn(user);

            Instant actualResetTime = rateLimitManager.getResetTime(user.getId());

            assertThat(actualResetTime).isEqualTo(expectedResetTime);
        }

        @Test
        @DisplayName("NULL resetTime은 현재 시각을 반환해야 함")
        void shouldReturnNowWhenResetTimeIsNull() {
            User user = createUserWithGithubAccount(1003L);
            Instant before = Instant.now();

            when(userRepository.getById(user.getId())).thenReturn(user);

            Instant resetTime = rateLimitManager.getResetTime(user.getId());

            Instant after = Instant.now();
            assertThat(resetTime).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("ApplicationReadyEvent 리스너 실행 테스트")
    class JobSchedulerInitializerTest {

        @Test
        @DisplayName("서버 시작 시 모든 사용자가 큐에 추가되어야 함")
        void shouldEnqueueAllUsersOnStartup() {
            User user1 = createUserWithGithubAccount(2001L);
            User user2 = createUserWithGithubAccount(2002L);
            
            when(userIdProvider.findActiveUserIds()).thenReturn(List.of(user1.getId(), user2.getId()));
            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

            jobSchedulerInitializer.initializeScheduler();

            verify(jobQueueService, times(2)).enqueue(any(Long.class), any(String.class), any(Instant.class), any(Priority.class));
        }

        @Test
        @DisplayName("GitHub 계정 없는 사용자는 큐에 추가되지 않아야 함")
        void shouldSkipUsersWithoutGithubAccount() {
            User userWithoutGithub = createUserWithoutGithub(9999L);
            User userWithGithub = createUserWithGithubAccount(2003L);
            
            when(userIdProvider.findActiveUserIds()).thenReturn(List.of(userWithoutGithub.getId(), userWithGithub.getId()));
            when(userRepository.findById(userWithoutGithub.getId())).thenReturn(Optional.of(userWithoutGithub));
            when(userRepository.findById(userWithGithub.getId())).thenReturn(Optional.of(userWithGithub));

            jobSchedulerInitializer.initializeScheduler();

            verify(jobQueueService, times(1)).enqueue(any(Long.class), any(String.class), any(Instant.class), any(Priority.class));
        }
    }

    @Nested
    @DisplayName("NULL Rate Limit 처리 테스트")
    class NullRateLimitHandlingTest {

        @Test
        @DisplayName("resetTime이 NULL인 사용자는 즉시 실행 큐에 추가 (HIGH priority)")
        void shouldEnqueueImmediatelyWhenResetTimeIsNull() {
            User user = createUserWithGithubAccount(3001L);
            
            when(userIdProvider.findActiveUserIds()).thenReturn(List.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            jobSchedulerInitializer.initializeScheduler();

            verify(jobQueueService).enqueue(eq(user.getId()), any(String.class), any(Instant.class), eq(Priority.HIGH));
        }

        @Test
        @DisplayName("isRateLimitExpired()는 NULL resetTime일 때 true를 반환해야 함")
        void shouldReturnTrueWhenResetTimeIsNull() {
            User user = createUserWithGithubAccount(3002L);

            GithubUser githubUser = user.getGithubUser();
            boolean isExpired = githubUser.isRateLimitExpired();

            assertThat(isExpired).isTrue();
        }
    }

    @Nested
    @DisplayName("Future Rate Limit 처리 테스트")
    class FutureRateLimitHandlingTest {

        @Test
        @DisplayName("resetTime이 미래인 사용자는 스케줄 큐에 추가 (LOW priority)")
        void shouldScheduleWhenResetTimeIsFuture() {
            User user = createUserWithGithubAccount(4001L);
            Instant futureResetTime = Instant.now().plus(1, ChronoUnit.HOURS);

            GithubUser githubUser = user.getGithubUser();
            githubUser.updateRateLimitResetTime(futureResetTime);

            when(userIdProvider.findActiveUserIds()).thenReturn(List.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            jobSchedulerInitializer.initializeScheduler();

            verify(jobQueueService).enqueue(eq(user.getId()), any(String.class), any(Instant.class), eq(Priority.LOW));
        }

        @Test
        @DisplayName("isRateLimitExpired()는 미래 resetTime일 때 false를 반환해야 함")
        void shouldReturnFalseWhenResetTimeIsFuture() {
            User user = createUserWithGithubAccount(4002L);
            Instant futureResetTime = Instant.now().plus(30, ChronoUnit.MINUTES);

            GithubUser githubUser = user.getGithubUser();
            githubUser.updateRateLimitResetTime(futureResetTime);

            boolean isExpired = githubUser.isRateLimitExpired();

            assertThat(isExpired).isFalse();
        }

        @Test
        @DisplayName("resetTime이 과거인 사용자는 즉시 실행 큐에 추가 (HIGH priority)")
        void shouldEnqueueImmediatelyWhenResetTimeIsPast() {
            User user = createUserWithGithubAccount(4003L);
            Instant pastResetTime = Instant.now().minus(10, ChronoUnit.MINUTES);

            GithubUser githubUser = user.getGithubUser();
            githubUser.updateRateLimitResetTime(pastResetTime);

            when(userIdProvider.findActiveUserIds()).thenReturn(List.of(user.getId()));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            jobSchedulerInitializer.initializeScheduler();

            verify(jobQueueService).enqueue(eq(user.getId()), any(String.class), any(Instant.class), eq(Priority.HIGH));
        }
    }

    @Nested
    @DisplayName("Backward Compatibility 테스트")
    class BackwardCompatibilityTest {

        @Test
        @DisplayName("기존 사용자는 rate limit 없이도 정상 동작해야 함")
        void shouldWorkWithoutRateLimitData() {
            User user = createUserWithGithubAccount(5001L);
            
            when(userRepository.getById(user.getId())).thenReturn(user);

            Instant resetTime = rateLimitManager.getResetTime(user.getId());

            assertThat(resetTime).isNotNull();
            assertThat(resetTime).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("waitIfNeeded()는 NULL resetTime일 때 에러 없이 실행되어야 함")
        void shouldNotThrowWhenWaitIfNeededWithNullResetTime() {
            User user = createUserWithGithubAccount(5002L);
            
            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.waitIfNeeded(user.getId(), 5000).block();

            GithubUser githubUser = user.getGithubUser();
            assertThat(githubUser.getRateLimitResetAt()).isNull();
        }

        @Test
        @DisplayName("새로운 rate limit 업데이트는 기존 데이터를 덮어쓰지 않아야 함")
        void shouldNotOverwriteUserDataOnRateLimitUpdate() {
            User user = createUserWithGithubAccount(5003L);
            String originalLogin = user.getGithubUser().getGithubLogin();
            long resetTimeMillis = Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli();

            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.updateRateLimitFromHeaders(user.getId(), resetTimeMillis);

            GithubUser githubUser = user.getGithubUser();
            assertThat(githubUser.getGithubLogin()).isEqualTo(originalLogin);
            assertThat(githubUser.getRateLimitResetAt()).isNotNull();
        }
    }

    private User createUserWithGithubAccount(Long githubId) {
        GithubUser githubUser = GithubUser.builder()
            .githubId(githubId)
            .githubLogin("user" + githubId)
            .githubName("User " + githubId)
            .githubToken("dummy_token_" + githubId)
            .githubAvatarUrl("https://avatar.url/" + githubId)
            .lastCrawling(LocalDateTime.now())
            .build();

        User user = User.builder()
            .id(githubId)
            .name("User " + githubId)
            .kutId(String.valueOf(githubId))
            .kutEmail("user" + githubId + "@koreatech.ac.kr")
            .password("tempPassword123!")
            .githubUser(githubUser)
            .build();
        return user;
    }

    private User createUserWithoutGithub(Long userId) {
        return User.builder()
            .id(userId)
            .name("User " + userId)
            .kutId(String.valueOf(userId))
            .kutEmail("user" + userId + "@koreatech.ac.kr")
            .password("tempPassword123!")
            .build();
    }
}
