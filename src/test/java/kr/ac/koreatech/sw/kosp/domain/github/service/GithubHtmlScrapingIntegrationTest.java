package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

/**
 * Integration tests for HTML Scraping
 * 
 * Uses real GitHub pages
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("HTML Scraping 통합 테스트")
class GithubHtmlScrapingIntegrationTest {
    
    @Autowired
    private GithubHtmlScrapingService service;
    
    @Test
    @DisplayName("PR/Issue 개수: 실제 저장소에서 추출")
    void scrapePRIssueCounts_withRealRepo_shouldExtractCounts() {
        // Given
        String owner = "octocat";
        String repo = "Hello-World";
        
        // When
        var result = service.scrapePRIssueCounts(owner, repo);
        
        // Then
        StepVerifier.create(result)
            .assertNext(counts -> {
                assertThat(counts).isNotNull();
                assertThat(counts).containsKeys("totalPRs", "totalIssues");
                System.out.println("PR/Issue counts: " + counts);
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("저장소 페이지: 실제 저장소에서 메트릭 추출")
    void scrapeRepoPage_withRealRepo_shouldExtractMetrics() {
        // Given
        String owner = "octocat";
        String repo = "Hello-World";
        
        // When
        var result = service.scrapeRepoPage(owner, repo);
        
        // Then
        StepVerifier.create(result)
            .assertNext(data -> {
                assertThat(data).isNotNull();
                System.out.println("Repo page data: " + data);
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("사용자 프로필: 실제 사용자에서 Achievements 추출")
    void scrapeUserProfile_withRealUser_shouldExtractAchievements() {
        // Given
        String githubId = "octocat";
        
        // When
        var result = service.scrapeUserProfile(githubId);
        
        // Then
        StepVerifier.create(result)
            .assertNext(profile -> {
                assertThat(profile).isNotNull();
                System.out.println("User profile: " + profile);
            })
            .verifyComplete();
    }
}
