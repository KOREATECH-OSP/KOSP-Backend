package io.swkoreatech.kosp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.swkoreatech.kosp.client.RateLimitException;
import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rate Limit Threshold Behavior Integration Test")
class RateLimitThresholdIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RateLimitManager rateLimitManager;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("임계값 경계 조건 테스트")
    class ThresholdBoundaryTest {

        @Test
        @DisplayName("임계값 미도달 시 (remaining=150, threshold=100) 예외 발생하지 않아야 함")
        void shouldNotThrowWhenRemainingAboveThreshold() {
            User user = createUserWithGithubAccount(1001L);
            GithubUser githubUser = user.getGithubUser();
            
            Instant futureResetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(futureResetTime, 150);

            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.waitIfNeeded(user.getId(), 100).block();

            assertThat(githubUser.getRemainingOrDefault()).isEqualTo(150);
        }

        @Test
        @DisplayName("임계값 정확히 도달 시 (remaining=100, threshold=100) RateLimitException 발생해야 함")
        void shouldThrowWhenRemainingEqualsThreshold() {
            User user = createUserWithGithubAccount(1002L);
            GithubUser githubUser = user.getGithubUser();
            
            Instant futureResetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(futureResetTime, 100);

            when(userRepository.getById(user.getId())).thenReturn(user);

            assertThatThrownBy(() -> rateLimitManager.waitIfNeeded(user.getId(), 100).block())
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Rate limit threshold reached");
        }

        @Test
        @DisplayName("임계값 초과 시 (remaining=50, threshold=100) RateLimitException 발생하고 대기 시간 포함해야 함")
        void shouldThrowWithWaitTimeWhenRemainingBelowThreshold() {
            User user = createUserWithGithubAccount(1003L);
            GithubUser githubUser = user.getGithubUser();
            
            Instant futureResetTime = Instant.now().plusSeconds(1800);
            githubUser.updateRateLimit(futureResetTime, 50);

            when(userRepository.getById(user.getId())).thenReturn(user);

            assertThatThrownBy(() -> rateLimitManager.waitIfNeeded(user.getId(), 100).block())
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Rate limit threshold reached")
                .hasMessageContaining("Reset at: " + futureResetTime);

            assertThat(githubUser.getRemainingOrDefault()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("NULL remaining 처리 테스트")
    class NullRemainingHandlingTest {

        @Test
        @DisplayName("remaining이 NULL일 때 기본값 5000 사용하여 예외 발생하지 않아야 함")
        void shouldUseDefaultValueWhenRemainingIsNull() {
            User user = createUserWithGithubAccount(2001L);
            GithubUser githubUser = user.getGithubUser();

            Instant futureResetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(futureResetTime, null);

            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.waitIfNeeded(user.getId(), 100).block();

            assertThat(githubUser.getRemainingOrDefault()).isEqualTo(5000);
        }

        @Test
        @DisplayName("remaining이 NULL이고 resetTime도 NULL일 때 예외 발생하지 않아야 함")
        void shouldNotThrowWhenBothRemainingAndResetTimeAreNull() {
            User user = createUserWithGithubAccount(2002L);
            GithubUser githubUser = user.getGithubUser();

            githubUser.updateRateLimit(null, null);

            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.waitIfNeeded(user.getId(), 100).block();

            assertThat(githubUser.getRemainingOrDefault()).isEqualTo(5000);
            assertThat(githubUser.getRateLimitResetAt()).isNull();
        }
    }

    @Nested
    @DisplayName("재시작 시뮬레이션 테스트")
    class RestartSimulationTest {

        @Test
        @DisplayName("remaining은 @Transient 필드이므로 재시작 시 손실되고 다음 API 호출에서 복구되어야 함")
        void shouldLoseRemainingOnRestartAndRecoverOnNextApiCall() {
            User user = createUserWithGithubAccount(3001L);
            GithubUser githubUser = user.getGithubUser();

            Instant resetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(resetTime, 150);

            assertThat(githubUser.getRateLimitRemaining()).isEqualTo(150);

            GithubUser simulatedReloadedUser = GithubUser.builder()
                .githubId(user.getGithubUser().getGithubId())
                .githubLogin(user.getGithubUser().getGithubLogin())
                .githubName(user.getGithubUser().getGithubName())
                .githubToken(user.getGithubUser().getGithubToken())
                .githubAvatarUrl(user.getGithubUser().getGithubAvatarUrl())
                .lastCrawling(user.getGithubUser().getLastCrawling())
                .rateLimitResetAt(resetTime)
                .build();

            assertThat(simulatedReloadedUser.getRateLimitRemaining()).isNull();
            assertThat(simulatedReloadedUser.getRateLimitResetAt()).isEqualTo(resetTime);
            assertThat(simulatedReloadedUser.getRemainingOrDefault()).isEqualTo(5000);

            simulatedReloadedUser.updateRateLimit(resetTime, 150);

            assertThat(simulatedReloadedUser.getRateLimitRemaining()).isEqualTo(150);
            assertThat(simulatedReloadedUser.getRemainingOrDefault()).isEqualTo(150);
        }
    }

    @Nested
    @DisplayName("임계값 101 경계 테스트")
    class ThresholdOneHundredOneTest {

        @Test
        @DisplayName("임계값 101일 때 remaining=102는 예외 발생하지 않아야 함")
        void shouldNotThrowWhenRemainingAboveThreshold101() {
            User user = createUserWithGithubAccount(4001L);
            GithubUser githubUser = user.getGithubUser();
            
            Instant futureResetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(futureResetTime, 102);

            when(userRepository.getById(user.getId())).thenReturn(user);

            rateLimitManager.waitIfNeeded(user.getId(), 101).block();

            assertThat(githubUser.getRemainingOrDefault()).isEqualTo(102);
        }

        @Test
        @DisplayName("임계값 101일 때 remaining=101은 RateLimitException 발생해야 함")
        void shouldThrowWhenRemainingEqualsThreshold101() {
            User user = createUserWithGithubAccount(4002L);
            GithubUser githubUser = user.getGithubUser();
            
            Instant futureResetTime = Instant.now().plusSeconds(3600);
            githubUser.updateRateLimit(futureResetTime, 101);

            when(userRepository.getById(user.getId())).thenReturn(user);

            assertThatThrownBy(() -> rateLimitManager.waitIfNeeded(user.getId(), 101).block())
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Rate limit threshold reached");
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
}
