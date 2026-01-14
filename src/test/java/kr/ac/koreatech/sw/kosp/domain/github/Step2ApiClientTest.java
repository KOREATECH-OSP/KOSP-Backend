package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.RateLimitException;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.RateLimitManager;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Step 2: GitHub API Client 테스트")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Step2ApiClientTest {

    @Autowired(required = false)
    private GithubRestApiClient restApiClient;

    @Autowired(required = false)
    private GithubGraphQLClient graphQLClient;

    @Autowired(required = false)
    private RateLimitManager rateLimitManager;

    @Value("${test.github.access-token:}")
    private String testToken;

    @Test
    @DisplayName("REST API Client 빈 로드 테스트")
    void testRestApiClientBean() {
        assertThat(restApiClient).isNotNull();
    }

    @Test
    @DisplayName("GraphQL Client 빈 로드 테스트")
    void testGraphQLClientBean() {
        assertThat(graphQLClient).isNotNull();
    }

    @Test
    @DisplayName("Rate Limit Manager 빈 로드 테스트")
    void testRateLimitManagerBean() {
        assertThat(rateLimitManager).isNotNull();
    }

    @Test
    @DisplayName("Rate Limit Manager 초기 상태 테스트")
    void testRateLimitManagerInitialState() {
        // Given & When
        int remaining = rateLimitManager.getRemainingRequests();

        // Then
        assertThat(remaining).isGreaterThan(0).isLessThanOrEqualTo(5000);
    }

    @Test
    @DisplayName("Rate Limit Manager 요청 기록 테스트")
    void testRateLimitManagerRecordRequest() {
        // Given
        int initialRemaining = rateLimitManager.getRemainingRequests();

        // When
        rateLimitManager.recordRequest();
        rateLimitManager.recordRequest();
        rateLimitManager.recordRequest();

        // Then
        int currentRemaining = rateLimitManager.getRemainingRequests();
        assertThat(currentRemaining).isLessThan(initialRemaining);
    }

    @Test
    @DisplayName("Rate Limit 소진 시 Bypass 기능 테스트")
    void testRateLimitBypass() {
        // Given: Rate Limit을 0으로 설정 (강제 소진)
        long futureReset = System.currentTimeMillis() + 3600000; // 1시간 후
        rateLimitManager.updateRateLimit(0, futureReset);
        
        // When & Then 1: 일반 get()은 실패해야 함
        try {
             restApiClient.get("/rate_limit", "dummy_token", java.util.Map.class)
                 .block();
        } catch (Exception e) {
            // Unwarp exception
            Throwable actualException = e;
            if (e.getCause() != null) {
                actualException = e.getCause();
            }
            assertThat(actualException).isInstanceOf(RateLimitException.class);
        }

        // When & Then 2: Bypass 메서드는 로컬 체크를 통과해야 함
        try {
            restApiClient.getWithoutRateLimitCheck("/rate_limit", "dummy_token", java.util.Map.class)
                .block();
        } catch (Exception e) {
            // Unwarp exception
            Throwable actualException = e;
            if (e.getCause() != null) {
                actualException = e.getCause();
            }
            // RateLimitException이 아니어야 함
            assertThat(actualException).isNotInstanceOf(RateLimitException.class);
        }
    }
}
