package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.RateLimitManager;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Step 2: GitHub API Client 테스트")
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
}
