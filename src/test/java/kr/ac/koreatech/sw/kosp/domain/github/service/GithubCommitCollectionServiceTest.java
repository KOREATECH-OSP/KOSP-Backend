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
 * Integration tests for GithubCommitCollectionService
 * 
 * Feature 1: Commits Author Filtering
 * 
 * Requires GITHUB_TOKEN environment variable
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GithubCommitCollectionService 통합 테스트")
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class GithubCommitCollectionServiceIntegrationTest {
    
    @Autowired
    private GithubCommitCollectionService service;
    
    @Value("${GITHUB_TOKEN:}")
    private String githubToken;
    
    @Test
    @DisplayName("작은 저장소: 모든 사용자 커밋 수집")
    void collectAllCommits_withSmallRepo_shouldCollectAllUserCommits() {
        // Given
        String owner = "octocat";
        String repo = "Hello-World";
        String author = "octocat";
        
        // When
        var result = service.collectAllCommits(owner, repo, author, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(count -> {
                assertThat(count).isGreaterThanOrEqualTo(0);
                System.out.println("✅ Collected " + count + " commits");
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("대형 저장소: 타임아웃 없이 완료 (Author 필터링)")
    void collectAllCommits_withLargeRepo_shouldNotTimeout() {
        // Given
        String owner = "JetBrains";
        String repo = "intellij-community";
        String author = "donnerpeter";  // Active contributor
        
        System.out.println("🚀 Testing large repository with author filtering");
        long startTime = System.currentTimeMillis();
        
        // When
        var result = service.collectAllCommits(owner, repo, author, githubToken);
        
        // Then - Should complete within reasonable time
        StepVerifier.create(result)
            .assertNext(count -> {
                long duration = System.currentTimeMillis() - startTime;
                
                assertThat(count).isGreaterThan(0);
                assertThat(duration).isLessThan(60_000);  // Less than 60 seconds
                
                System.out.println("✅ Large repository collection completed");
                System.out.println("   Commits collected: " + count);
                System.out.println("   Duration: " + (duration / 1000) + " seconds");
                System.out.println("   No PrematureCloseException!");
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("빈 결과: 존재하지 않는 사용자")
    void collectAllCommits_withNonExistentAuthor_shouldReturnZero() {
        // Given
        String owner = "octocat";
        String repo = "Hello-World";
        String author = "nonexistent-user-12345";
        
        // When
        var result = service.collectAllCommits(owner, repo, author, githubToken);
        
        // Then
        StepVerifier.create(result)
            .expectNext(0L)
            .verifyComplete();
    }
}
