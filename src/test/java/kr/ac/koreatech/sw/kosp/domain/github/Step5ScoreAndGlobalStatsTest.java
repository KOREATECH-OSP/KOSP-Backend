package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubGlobalStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubGlobalStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubScoreCalculator;
import kr.ac.koreatech.sw.kosp.domain.github.service.GlobalStatisticsCalculator;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Step 5: 신규 점수 로직 및 글로벌 통계 테스트")
class Step5ScoreAndGlobalStatsTest {

    @Autowired
    private GithubScoreCalculator scoreCalculator;

    @Autowired
    private GlobalStatisticsCalculator globalStatisticsCalculator;

    @Autowired
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Autowired
    private GithubGlobalStatisticsRepository globalStatisticsRepository;

    @Autowired
    private GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    @Autowired
    private GithubPRRawRepository prRawRepository; // Fixed: Use singular Raw Repository

    @Autowired
    private GithubCommitDetailRawRepository commitRawRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        if (mongoTemplate != null) {
            mongoTemplate.dropCollection(GithubPRRaw.class);
            mongoTemplate.dropCollection(GithubCommitDetailRaw.class);
        }
    }

    @Test
    @DisplayName("활동 점수(Activity Score) - High Activity")
    void testActivityScoreHigh() {
        // Given
        String githubId = "high_activity_user";
        createMockUserStats(githubId, 200, 0, 50, 20, 1); // 200 commits -> Max Activity
        createRepoStats(githubId, githubId, "repo1", 200, 50, true, 0); // 1 repo

        // When
        BigDecimal score = scoreCalculator.calculate(githubId);

        // Then
        // Activity: 3.0 (Max)
        // Diversity: 0.0 (1 repo < 2)
        // Impact: 0.0
        assertThat(score).isEqualByComparingTo(BigDecimal.valueOf(3.0));
    }

    @Test
    @DisplayName("다양성 점수(Diversity Score) - 10+ Repos")
    void testDiversityScoreHigh() {
        // Given
        String githubId = "diversity_user";
        // Activity Score: Need 100 Commits + 20 PRs in ONE repo for 3.0.
        // Diversity Score: Need 10 contributed repos in userMock for 1.0.
        createMockUserStats(githubId, 100, 0, 20, 0, 10); 
        
        // 1. One "Main" Repo for Max Activity (3.0)
        createRepoStats(githubId, "main_owner", "main_repo", 100, 20, true, 0);
        
        // 2. 9 "Other" Repos for Diversity (Just presence)
        for (int i = 1; i <= 9; i++) {
            createRepoStats(githubId, "owner" + i, "repo" + i, 1, 1, false, 0);
        }

        // When
        BigDecimal score = scoreCalculator.calculate(githubId);

        // Then
        // Activity: 3.0
        // Diversity: 1.0
        // Impact: 0.0
        assertThat(score).isEqualByComparingTo(BigDecimal.valueOf(4.0));
    }

    @Test
    @DisplayName("영향력 점수(Impact Score) - Owned Repo Stars & External PR")
    void testImpactScore() {
        // Given
        String githubId = "impact_user";
        createMockUserStats(githubId, 100, 0, 20, 0, 1);
        
        // 1. Owned Repo with 150 Stars (+2.0 points)
        createRepoStats(githubId, githubId, "my-star-repo", 100, 20, true, 150);

        // 2. PR to External Repo with 2000 Stars (+1.5 points)
        GithubPRRaw pr = GithubPRRaw.builder()
            .repoOwner("external")
            .repoName("huge-repo")
            .prNumber(1) // Fixed: number -> prNumber
            .prData(Map.of(
                "merged_at", "2024-01-01T00:00:00Z",
                "merged", true,
                "state", "closed", // Included in map just in case
                "title", "Fix bug",
                "user", Map.of("login", githubId),
                "base", Map.of(
                    "repo", Map.of(
                        "stargazers_count", 2000,
                        "owner", Map.of("login", "external"),
                        "fork", false
                    )
                )
            ))
            .build();
        prRawRepository.save(pr).block(); // Block for reactive save

        // When
        BigDecimal score = scoreCalculator.calculate(githubId);

        // Then
        // Activity: 100 commits, 20 prs -> 3.0
        // Diversity: 1 repo (my-star-repo) -> 0.0 (need >= 2)
        // Impact: 2.0 (Stars) + 1.5 (External PR) = 3.5
        // Total = 3.0 + 0.0 + 3.5 = 6.5
        assertThat(score).isEqualByComparingTo(BigDecimal.valueOf(6.5));
    }

    @Test
    @DisplayName("글로벌 통계 계산 테스트")
    void testGlobalStatistics() {
        // Given
        createMockUserStats("user1", 100, 50, 10, 5, 1); // C:100, S:50, P:10, I:5
        createMockUserStats("user2", 200, 150, 20, 15, 2); // C:200, S:150, P:20, I:15
        
        // When
        globalStatisticsCalculator.calculateAndSave();

        // Then
        GithubGlobalStatistics global = globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()
            .orElseThrow();
        
        // Check Averages
        // Commits: (100+200)/2 = 150
        assertThat(global.getAvgCommitCount()).isEqualTo(150.0);
        
        // Stars: (50+150)/2 = 100
        assertThat(global.getAvgStarCount()).isEqualTo(100.0);

        // PRs: (10+20)/2 = 15
        assertThat(global.getAvgPrCount()).isEqualTo(15.0);

        // Issues: (5+15)/2 = 10
        assertThat(global.getAvgIssueCount()).isEqualTo(10.0);

        // Total Users
        assertThat(global.getTotalUsers()).isEqualTo(2);
    }

    private void createRepoStats(String githubId, String owner, String name, int commits, int prs, boolean isOwned, int stars) {
        GithubRepositoryStatistics stat = GithubRepositoryStatistics.create(owner, name, githubId);
        stat.updateUserContributions(commits, prs, 0, LocalDateTime.now());
        stat.updateRepositoryInfo(stars, 0, 0, "desc", "Java", LocalDateTime.now());
        stat.updateOwnership(isOwned);
        repositoryStatisticsRepository.save(stat);
    }

    private void createMockUserStats(String githubId, int commits, int stars, int prs, int issues, int contributedRepos) {
        GithubUserStatistics stats = GithubUserStatistics.create(githubId);
        stats.updateStatistics(
            commits, 0, 0, 0, prs, issues, 
            0, contributedRepos, stars, 0, 0, 0
        );
        userStatisticsRepository.save(stats);
    }
}
