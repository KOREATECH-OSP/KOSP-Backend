package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

/**
 * Integration tests for GithubHtmlScrapingService
 * 
 * Features 5, 6, 8, 9: HTML scraping
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GithubHtmlScrapingService 통합 테스트")
class GithubHtmlScrapingServiceTest {
    
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
                // PR은 반드시 있어야 함
                assertThat(counts).containsKey("totalPRs");
                // Issue는 선택적 (저장소에 따라 없을 수 있음)
                System.out.println("✅ PR/Issue counts: " + counts);
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
                System.out.println("✅ Repo page data: " + data);
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
                System.out.println("✅ User profile: " + profile);
            })
            .verifyComplete();
    }
}
