package kr.ac.koreatech.sw.kosp.domain.github.e2e;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.service.GithubCommitCollectionService;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

/**
 * E2E tests for large repository collection
 * 
 * Verifies that large repositories like intellij-community can be collected
 * without timeout or memory issues
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Large Repository E2E 테스트")
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class LargeRepositoryE2ETest {
    
    @Autowired
    private GithubCommitCollectionService commitCollectionService;
    
    @Value("${GITHUB_TOKEN:}")
    private String githubToken;
    
    @Test
    @DisplayName("intellij-community: 타임아웃 없이 완료")
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void collectIntellijCommunity_shouldCompleteWithoutTimeout() {
        // Given
        String owner = "JetBrains";
        String repo = "intellij-community";
        String author = "donnerpeter";  // Active contributor with reasonable commit count
        
        System.out.println("🚀 Starting large repository collection test");
        System.out.println("   Repository: " + owner + "/" + repo);
        System.out.println("   Author: " + author);
        
        long startTime = System.currentTimeMillis();
        
        // When
        var result = commitCollectionService.collectAllCommits(owner, repo, author, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(count -> {
                long duration = System.currentTimeMillis() - startTime;
                
                assertThat(count).isGreaterThan(0);
                assertThat(duration).isLessThan(300_000);  // Less than 5 minutes
                
                System.out.println("✅ Large repository collection completed");
                System.out.println("   Commits collected: " + count);
                System.out.println("   Duration: " + (duration / 1000) + " seconds");
                System.out.println("   No PrematureCloseException!");
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("대형 저장소: 메모리 사용량 모니터링")
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void collectLargeRepo_shouldNotExceedMemoryLimit() {
        // Given
        String owner = "torvalds";
        String repo = "linux";
        String author = "torvalds";
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("🔍 Memory before: " + (memoryBefore / 1024 / 1024) + " MB");
        
        // When
        var result = commitCollectionService.collectAllCommits(owner, repo, author, githubToken);
        
        // Then
        StepVerifier.create(result)
            .assertNext(count -> {
                long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                long memoryUsed = memoryAfter - memoryBefore;
                
                System.out.println("🔍 Memory after: " + (memoryAfter / 1024 / 1024) + " MB");
                System.out.println("🔍 Memory used: " + (memoryUsed / 1024 / 1024) + " MB");
                
                assertThat(count).isGreaterThan(0);
                // Memory usage should be reasonable (< 500MB increase)
                assertThat(memoryUsed).isLessThan(500 * 1024 * 1024);
            })
            .verifyComplete();
    }
}
