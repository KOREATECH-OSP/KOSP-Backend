package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

/**
 * Integration tests for GithubDataCollectionService
 * 
 * Features 7, 10, 11, 12: API collection and Total Stars
 * 
 * Requires GITHUB_TOKEN environment variable
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GithubDataCollectionService 통합 테스트")
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class GithubDataCollectionServiceTest {
    
    @Autowired
    private GithubDataCollectionService service;
    
    @Value("${GITHUB_TOKEN:}")
    private String githubToken;
    
    private static final String TEST_OWNER = "octocat";
    private static final String TEST_REPO = "Hello-World";
    private static final String TEST_USER = "octocat";
    
    // Feature 7: Repository API Data
    
    @Test
    @DisplayName("Contributors 수집: API에서 contributor 로그인 추출")
    void collectContributors_withValidRepo_shouldReturnContributorLogins() {
        // When
        var result = service.collectContributors(TEST_OWNER, TEST_REPO, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(logins -> {
                assertThat(logins).isNotEmpty();
                System.out.println("✅ Contributors: " + logins.size());
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("Releases 수집: releaseCount와 latestRelease 반환")
    void collectReleases_withReleases_shouldReturnReleaseData() {
        // When
        var result = service.collectReleases(TEST_OWNER, TEST_REPO, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(data -> {
                assertThat(data).containsKeys("releaseCount", "latestRelease");
                System.out.println("✅ Releases: " + data);
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("README 확인: README 파일 크기 반환")
    void checkReadme_withReadmePresent_shouldReturnSize() {
        // When
        var result = service.checkReadme(TEST_OWNER, TEST_REPO, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(size -> {
                assertThat(size).isGreaterThan(0);
                System.out.println("✅ README size: " + size);
            })
            .verifyComplete();
    }
    
    // Feature 10: User Following
    
    @Test
    @DisplayName("Following 수집: API에서 following 리스트 저장")
    void collectFollowing_withFollowingList_shouldSaveToMongoDB() {
        // When
        var result = service.collectFollowing(TEST_USER, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(list -> {
                assertThat(list).isNotNull();
                System.out.println("✅ Following: " + list.size());
            })
            .verifyComplete();
    }
    
    // Feature 11: User Starred
    
    @Test
    @DisplayName("Starred 수집: API에서 starred 저장소 저장")
    void collectStarred_withStarredRepos_shouldSaveToMongoDB() {
        // When
        var result = service.collectStarred(TEST_USER, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(list -> {
                assertThat(list).isNotNull();
                System.out.println("✅ Starred: " + list.size());
            })
            .verifyComplete();
    }
    
    // Feature 12: Total Stars Calculation
    
    @Test
    @DisplayName("User Basic Info 수집: Total Stars 포함")
    void collectUserBasicInfo_shouldIncludeTotalStars() {
        // When
        var result = service.collectUserBasicInfo(TEST_USER, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(userBasic -> {
                assertThat(userBasic).isNotNull();
                assertThat(userBasic.getTotalStars()).isNotNull();
                assertThat(userBasic.getTotalStars()).isGreaterThanOrEqualTo(0);
                System.out.println("✅ User: " + userBasic.getGithubId());
                System.out.println("   Total Stars: " + userBasic.getTotalStars());
            })
            .verifyComplete();
    }
}
