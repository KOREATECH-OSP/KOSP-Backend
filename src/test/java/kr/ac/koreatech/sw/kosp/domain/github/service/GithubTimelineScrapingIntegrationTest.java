package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubTimelineDataRepository;
import reactor.test.StepVerifier;

/**
 * Integration tests for Timeline Scraping
 * 
 * Note: These tests use real GitHub pages (small test accounts)
 * Requires internet connection
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Timeline Scraping 통합 테스트")
class GithubTimelineScrapingIntegrationTest {
    
    @Autowired
    private GithubTimelineScrapingService service;
    
    @Autowired
    private GithubTimelineDataRepository repository;
    
    @Test
    @DisplayName("Timeline 스크래핑: 실제 GitHub 페이지에서 데이터 추출")
    void scrapeTimeline_withRealGitHubPage_shouldExtractData() {
        // Given
        String testUser = "octocat";  // GitHub's test account
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 31);
        
        // Clean up before test
        repository.deleteAll();
        
        // When
        var result = service.scrapeTimeline(testUser, fromDate, toDate);
        
        // Then
        StepVerifier.create(result)
            .assertNext(data -> {
                assertThat(data).isNotNull();
                assertThat(data.getGithubId()).isEqualTo(testUser);
                assertThat(data.getFromDate()).isEqualTo(fromDate);
                assertThat(data.getToDate()).isEqualTo(toDate);
                // May have issues, PRs, or commits depending on timeline
            })
            .verifyComplete();
    }
    
    @Test
    @DisplayName("Timeline 스크래핑: 이미 수집된 데이터는 스킵")
    void scrapeTimeline_withAlreadyCollected_shouldSkipDuplicate() {
        // Given
        String testUser = "octocat";
        LocalDate fromDate = LocalDate.of(2024, 2, 1);
        LocalDate toDate = LocalDate.of(2024, 2, 28);
        
        // First collection
        service.scrapeTimeline(testUser, fromDate, toDate).block();
        
        // When - Second collection
        var result = service.scrapeTimeline(testUser, fromDate, toDate);
        
        // Then - Should return null (skipped)
        StepVerifier.create(result)
            .verifyComplete();
    }
}
