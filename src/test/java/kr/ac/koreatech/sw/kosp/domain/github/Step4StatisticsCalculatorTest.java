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

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import kr.ac.koreatech.sw.kosp.domain.github.service.MonthlyStatisticsCalculator;
import kr.ac.koreatech.sw.kosp.domain.github.service.UserStatisticsCalculator;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Step 4: 통계 계산 로직 테스트")
class Step4StatisticsCalculatorTest {

    @Autowired(required = false)
    private UserStatisticsCalculator userStatisticsCalculator;

    @Autowired(required = false)
    private MonthlyStatisticsCalculator monthlyStatisticsCalculator;

    @Autowired(required = false)
    private GithubStatisticsService githubStatisticsService;

    @Autowired(required = false)
    private GithubCommitDetailRawRepository commitDetailRawRepository;

    @Autowired(required = false)
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Autowired(required = false)
    private GithubMonthlyStatisticsRepository monthlyStatisticsRepository;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // MongoDB 데이터 정리
        if (mongoTemplate != null) {
            mongoTemplate.dropCollection(GithubCommitDetailRaw.class);
        }
    }

    @Test
    @DisplayName("Calculator 빈 로드 테스트")
    void testCalculatorBeans() {
        assertThat(userStatisticsCalculator).isNotNull();
        assertThat(monthlyStatisticsCalculator).isNotNull();
        assertThat(githubStatisticsService).isNotNull();
    }

    @Test
    @DisplayName("사용자 통계 계산 테스트 - 커밋 데이터 기반")
    void testUserStatisticsCalculation() {
        // Given: MongoDB에 테스트 커밋 데이터 저장
        String githubId = "testuser";
        
        GithubCommitDetailRaw commit1 = GithubCommitDetailRaw.builder()
            .sha("sha1")
            .repoOwner("testuser")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", "2024-01-15T14:30:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 100, "deletions", 50))
            .message("Test commit 1")
            .collectedAt(LocalDateTime.now())
            .build();

        GithubCommitDetailRaw commit2 = GithubCommitDetailRaw.builder()
            .sha("sha2")
            .repoOwner("testuser")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", "2024-01-20T23:30:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 200, "deletions", 100))
            .message("Test commit 2")
            .collectedAt(LocalDateTime.now())
            .build();

        GithubCommitDetailRaw commit3 = GithubCommitDetailRaw.builder()
            .sha("sha3")
            .repoOwner("testuser")
            .repoName("repo2")
            .author(Map.of("login", githubId, "date", "2024-02-10T10:00:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 150, "deletions", 75))
            .message("Test commit 3")
            .collectedAt(LocalDateTime.now())
            .build();

        commitDetailRawRepository.save(commit1);
        commitDetailRawRepository.save(commit2);
        commitDetailRawRepository.save(commit3);

        // When: 통계 계산
        GithubUserStatistics statistics = userStatisticsCalculator.calculate(githubId);

        // Then: 통계 검증
        assertThat(statistics).isNotNull();
        assertThat(statistics.getGithubId()).isEqualTo(githubId);
        assertThat(statistics.getTotalCommits()).isEqualTo(3);
        assertThat(statistics.getTotalAdditions()).isEqualTo(450); // 100 + 200 + 150
        assertThat(statistics.getTotalDeletions()).isEqualTo(225); // 50 + 100 + 75
        assertThat(statistics.getTotalLines()).isEqualTo(675); // 450 + 225
        assertThat(statistics.getOwnedReposCount()).isEqualTo(2); // repo1, repo2
        assertThat(statistics.getNightCommits()).isEqualTo(1); // commit2 (23:30)
        assertThat(statistics.getDayCommits()).isEqualTo(2); // commit1, commit3
        assertThat(statistics.getTotalScore()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("월별 통계 계산 테스트")
    void testMonthlyStatisticsCalculation() {
        // Given: MongoDB에 여러 달의 커밋 데이터 저장
        String githubId = "testuser";

        // 2024년 1월 커밋 2개
        commitDetailRawRepository.save(GithubCommitDetailRaw.builder()
            .sha("sha1")
            .repoOwner("testuser")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", "2024-01-15T14:30:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 100, "deletions", 50))
            .message("Jan commit 1")
            .collectedAt(LocalDateTime.now())
            .build());

        commitDetailRawRepository.save(GithubCommitDetailRaw.builder()
            .sha("sha2")
            .repoOwner("testuser")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", "2024-01-20T10:00:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 200, "deletions", 100))
            .message("Jan commit 2")
            .collectedAt(LocalDateTime.now())
            .build());

        // 2024년 2월 커밋 1개
        commitDetailRawRepository.save(GithubCommitDetailRaw.builder()
            .sha("sha3")
            .repoOwner("testuser")
            .repoName("repo2")
            .author(Map.of("login", githubId, "date", "2024-02-10T10:00:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 150, "deletions", 75))
            .message("Feb commit")
            .collectedAt(LocalDateTime.now())
            .build());

        // When: 월별 통계 계산
        List<GithubMonthlyStatistics> monthlyStats = monthlyStatisticsCalculator.calculate(githubId);

        // Then: 월별 통계 검증
        assertThat(monthlyStats).hasSize(2);

        // 1월 통계 찾기
        GithubMonthlyStatistics janStats = monthlyStats.stream()
            .filter(s -> s.getYear() == 2024 && s.getMonth() == 1)
            .findFirst()
            .orElseThrow();

        assertThat(janStats.getCommitsCount()).isEqualTo(2);
        assertThat(janStats.getAdditionsCount()).isEqualTo(300);
        assertThat(janStats.getDeletionsCount()).isEqualTo(150);
        assertThat(janStats.getLinesCount()).isEqualTo(450);

        // 2월 통계 찾기
        GithubMonthlyStatistics febStats = monthlyStats.stream()
            .filter(s -> s.getYear() == 2024 && s.getMonth() == 2)
            .findFirst()
            .orElseThrow();

        assertThat(febStats.getCommitsCount()).isEqualTo(1);
        assertThat(febStats.getAdditionsCount()).isEqualTo(150);
        assertThat(febStats.getDeletionsCount()).isEqualTo(75);
        assertThat(febStats.getLinesCount()).isEqualTo(225);
    }

    @Test
    @DisplayName("통계 서비스 통합 테스트 - 계산 및 저장")
    void testStatisticsServiceIntegration() {
        // Given: MongoDB에 커밋 데이터 저장
        String githubId = "integrationtest";

        commitDetailRawRepository.save(GithubCommitDetailRaw.builder()
            .sha("sha1")
            .repoOwner("integrationtest")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", "2024-01-15T14:30:00"))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 100, "deletions", 50))
            .message("Test commit")
            .collectedAt(LocalDateTime.now())
            .build());

        // When: 통계 계산 및 저장
        GithubUserStatistics savedStats = githubStatisticsService.calculateAndSaveUserStatistics(githubId);

        // Then: MySQL에 저장된 통계 검증
        assertThat(savedStats).isNotNull();
        assertThat(savedStats.getId()).isNotNull();
        assertThat(savedStats.getGithubId()).isEqualTo(githubId);
        assertThat(savedStats.getTotalCommits()).isEqualTo(1);

        // DB에서 다시 조회
        GithubUserStatistics found = userStatisticsRepository.findByGithubId(githubId).orElseThrow();
        assertThat(found.getTotalCommits()).isEqualTo(1);
        assertThat(found.getTotalLines()).isEqualTo(150);
    }

    @Test
    @DisplayName("점수 계산 정확성 테스트")
    void testScoreCalculation() {
        // Given
        String githubId = "scoretest";

        commitDetailRawRepository.save(GithubCommitDetailRaw.builder()
            .sha("sha1")
            .repoOwner("scoretest")
            .repoName("repo1")
            .author(Map.of("login", githubId, "date", LocalDateTime.now().toString()))
            .committer(Map.of("login", githubId))
            .stats(Map.of("additions", 1000, "deletions", 500))
            .message("Test")
            .collectedAt(LocalDateTime.now())
            .build());

        // When
        GithubUserStatistics statistics = userStatisticsCalculator.calculate(githubId);

        // Then
        // 점수 = (커밋 * 10) + (라인 * 0.01)
        // = (1 * 10) + (1500 * 0.01) = 10 + 15 = 25
        BigDecimal expectedScore = BigDecimal.valueOf(10)
            .add(BigDecimal.valueOf(1500 * 0.01));

        assertThat(statistics.getTotalScore()).isEqualByComparingTo(expectedScore);
    }
}
