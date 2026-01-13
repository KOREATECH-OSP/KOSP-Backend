package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubRepoContribute;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineIssue;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelinePR;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubTimelineDataRepository;
import reactor.test.StepVerifier;

/**
 * Integration tests for GithubTimelineScrapingService
 * 
 * Features 2, 3, 4: Timeline scraping and RepoContribute extraction
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GithubTimelineScrapingService 통합 테스트")
class GithubTimelineScrapingServiceTest {
    
    @Autowired
    private GithubTimelineScrapingService service;
    
    @Autowired
    private GithubTimelineDataRepository repository;
    
    private static final String TEST_GITHUB_ID = "testuser";
    private static final LocalDate TEST_FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TEST_TO_DATE = LocalDate.of(2024, 12, 31);
    
    @Test
    @DisplayName("Timeline 스크래핑: 실제 GitHub 페이지에서 데이터 추출")
    void scrapeTimeline_withRealGitHubPage_shouldExtractData() {
        // Given
        String testUser = "octocat";
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
                System.out.println("✅ Timeline scraped successfully");
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
            .expectNext((GithubTimelineData) null)
            .verifyComplete();
    }
    
    @Test
    @DisplayName("RepoContribute 추출: Timeline 데이터에서 고유 저장소 추출")
    void extractRepoContributes_withTimelineData_shouldExtractUniqueRepos() {
        // Given
        List<GithubTimelineIssue> issues = List.of(
            createIssue("owner1", "repo1", true),
            createIssue("owner2", "repo2", false),
            createIssue("owner1", "repo1", true)  // duplicate
        );
        
        List<GithubTimelinePR> prs = List.of(
            createPR("owner3", "repo3", false),
            createPR("owner2", "repo2", false)  // duplicate
        );
        
        GithubTimelineData timelineData = GithubTimelineData.create(
            TEST_GITHUB_ID, TEST_FROM_DATE, TEST_TO_DATE, issues, prs, 10
        );
        
        // When
        List<GithubRepoContribute> result = service.extractRepoContributes(timelineData);
        
        // Then
        assertThat(result).hasSize(3);  // 3 unique repos
        assertThat(result).extracting(GithubRepoContribute::getOwnerId)
            .containsExactlyInAnyOrder("owner1", "owner2", "owner3");
    }
    
    @Test
    @DisplayName("RepoContribute 추출: 중복 저장소는 제거")
    void extractRepoContributes_withDuplicateRepos_shouldDeduplicateByKey() {
        // Given
        List<GithubTimelineIssue> issues = List.of(
            createIssue("owner1", "repo1", true),
            createIssue("owner1", "repo1", true),
            createIssue("owner1", "repo1", true)
        );
        
        List<GithubTimelinePR> prs = List.of(
            createPR("owner1", "repo1", true),
            createPR("owner1", "repo1", true)
        );
        
        GithubTimelineData timelineData = GithubTimelineData.create(
            TEST_GITHUB_ID, TEST_FROM_DATE, TEST_TO_DATE, issues, prs, 5
        );
        
        // When
        List<GithubRepoContribute> result = service.extractRepoContributes(timelineData);
        
        // Then
        assertThat(result).hasSize(1);  // Only 1 unique repo
        assertThat(result.get(0).getOwnerId()).isEqualTo("owner1");
        assertThat(result.get(0).getRepoName()).isEqualTo("repo1");
    }
    
    // Helper methods
    private GithubTimelineIssue createIssue(String ownerId, String repoName, boolean isOwned) {
        return GithubTimelineIssue.builder()
            .githubId(TEST_GITHUB_ID)
            .ownerId(ownerId)
            .repoName(repoName)
            .title("Test Issue")
            .number(1)
            .date(LocalDate.now())
            .isOwnedRepo(isOwned)
            .build();
    }
    
    private GithubTimelinePR createPR(String ownerId, String repoName, boolean isOwned) {
        return GithubTimelinePR.builder()
            .githubId(TEST_GITHUB_ID)
            .ownerId(ownerId)
            .repoName(repoName)
            .title("Test PR")
            .number(1)
            .date(LocalDate.now())
            .isOwnedRepo(isOwned)
            .build();
    }
}
