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
 * Integration tests for Commit Collection
 * 
 * Requires GITHUB_TOKEN environment variable
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Commit Collection 통합 테스트")
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class GithubCommitCollectionIntegrationTest {
    
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
                assertThat(count).isGreaterThan(0);
                System.out.println("Collected " + count + " commits");
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("대형 저장소: 타임아웃 없이 완료")
    void collectAllCommits_withLargeRepo_shouldNotTimeout() {
        // Given
        String owner = "JetBrains";
        String repo = "intellij-community";
        String author = "donnerpeter";  // Active contributor
        
        // When
        var result = service.collectAllCommits(owner, repo, author, githubToken);
        
        // Then - Should complete within reasonable time (60 seconds)
        StepVerifier.create(result)
            .assertNext(count -> {
                assertThat(count).isGreaterThan(0);
                System.out.println("Collected " + count + " commits from large repo");
            })
            .verifyComplete();
    }
}
