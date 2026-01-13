package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)  // Reset DB after each test
@DisplayName("Step 1: 기반 구조 설정 테스트")
class Step1InfrastructureTest {

    @Autowired(required = false)
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Autowired(required = false)
    private GithubMonthlyStatisticsRepository monthlyStatisticsRepository;

    @Test
    @DisplayName("프로젝트 빌드 및 컨텍스트 로드 테스트")
    void contextLoads() {
        // Given & When & Then
        assertThat(userStatisticsRepository).isNotNull();
        assertThat(monthlyStatisticsRepository).isNotNull();
    }

    @Test
    @DisplayName("MySQL 연결 및 GithubUserStatistics 저장/조회 테스트")
    void testMySQLConnection_UserStatistics() {
        // Given
        String githubId = "testuser";
        GithubUserStatistics statistics = GithubUserStatistics.create(githubId);
        statistics.updateStatistics(
            100, 5000, 3000, 2000,
            10, 5,
            5, 10, 50, 15,
            20, 80
        );
        statistics.updateScore(BigDecimal.valueOf(1000.50));

        // When
        GithubUserStatistics saved = userStatisticsRepository.save(statistics);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGithubId()).isEqualTo(githubId);
        assertThat(saved.getTotalCommits()).isEqualTo(100);
        assertThat(saved.getTotalLines()).isEqualTo(5000);
        assertThat(saved.getTotalScore()).isEqualByComparingTo(BigDecimal.valueOf(1000.50));

        // 조회 테스트
        GithubUserStatistics found = userStatisticsRepository.findByGithubId(githubId).orElseThrow();
        assertThat(found.getGithubId()).isEqualTo(githubId);
        assertThat(found.getTotalCommits()).isEqualTo(100);
    }

    @Test
    @DisplayName("MySQL 연결 및 GithubMonthlyStatistics 저장/조회 테스트")
    void testMySQLConnection_MonthlyStatistics() {
        // Given
        String githubId = "testuser";
        GithubMonthlyStatistics statistics = GithubMonthlyStatistics.create(githubId, 2024, 1);
        statistics.updateStatistics(
            50, 2500, 1500, 1000,
            5, 3,
            2, 5
        );

        // When
        GithubMonthlyStatistics saved = monthlyStatisticsRepository.save(statistics);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGithubId()).isEqualTo(githubId);
        assertThat(saved.getYear()).isEqualTo(2024);
        assertThat(saved.getMonth()).isEqualTo(1);
        assertThat(saved.getCommitsCount()).isEqualTo(50);

        // 조회 테스트
        GithubMonthlyStatistics found = monthlyStatisticsRepository
            .findByGithubIdAndYearAndMonth(githubId, 2024, 1)
            .orElseThrow();
        assertThat(found.getCommitsCount()).isEqualTo(50);
    }

    // GithubCollectionMetadata는 이제 MongoDB 전용이므로 MySQL 테스트에서 제거됨

    @Test
    @DisplayName("순위 조회 테스트 (totalScore 내림차순)")
    void testRankingQuery() {
        // Given
        GithubUserStatistics stats = GithubUserStatistics.create("testuser");
        stats.updateStatistics(100, 5000, 3000, 2000, 10, 5, 3, 2, 50, 15, 20, 80);
        stats.updateScore(BigDecimal.valueOf(1500.50));

        GithubUserStatistics user2 = GithubUserStatistics.create("user2");
        user2.updateStatistics(150, 7500, 4500, 3000, 15, 8, 5, 3, 75, 25, 30, 120);
        user2.updateScore(BigDecimal.valueOf(2250.75));

        GithubUserStatistics user3 = GithubUserStatistics.create("user3");
        user3.updateStatistics(80, 3000, 1500, 1500, 5, 3, 2, 1, 30, 10, 15, 65);
        user3.updateScore(BigDecimal.valueOf(1000.25));

        userStatisticsRepository.save(stats);
        userStatisticsRepository.save(user2);
        userStatisticsRepository.save(user3);

        // When
        var rankings = userStatisticsRepository.findAllByOrderByTotalScoreDesc();

        // Then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getTotalScore()).isGreaterThan(rankings.get(1).getTotalScore());
        assertThat(rankings.get(1).getTotalScore()).isGreaterThan(rankings.get(2).getTotalScore());
    }
}
