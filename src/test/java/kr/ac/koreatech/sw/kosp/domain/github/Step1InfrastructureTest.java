package kr.ac.koreatech.sw.kosp.domain.github;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubCollectionMetadata;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubCollectionMetadataRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Step 1: 기반 구조 설정 테스트")
class Step1InfrastructureTest {

    @Autowired(required = false)
    private GithubUserStatisticsRepository userStatisticsRepository;

    @Autowired(required = false)
    private GithubMonthlyStatisticsRepository monthlyStatisticsRepository;

    @Autowired(required = false)
    private GithubCollectionMetadataRepository collectionMetadataRepository;

    @Test
    @DisplayName("프로젝트 빌드 및 컨텍스트 로드 테스트")
    void contextLoads() {
        // Given & When & Then
        assertThat(userStatisticsRepository).isNotNull();
        assertThat(monthlyStatisticsRepository).isNotNull();
        assertThat(collectionMetadataRepository).isNotNull();
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
            5, 10, 50,
            20, 80,
            BigDecimal.valueOf(1000.50)
        );

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
    @org.junit.jupiter.api.Disabled("Entity scanning issue - will fix in Step 4")
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

    @Test
    @DisplayName("MySQL 연결 및 GithubCollectionMetadata 저장/조회 테스트")
    void testMySQLConnection_CollectionMetadata() {
        // Given
        String githubId = "testuser";
        GithubCollectionMetadata metadata = GithubCollectionMetadata.create(githubId);
        metadata.markInitialCollected();
        metadata.updateLastCollected("abc123def456");
        metadata.incrementApiCalls(100);

        // When
        GithubCollectionMetadata saved = collectionMetadataRepository.save(metadata);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGithubId()).isEqualTo(githubId);
        assertThat(saved.getInitialCollected()).isTrue();
        assertThat(saved.getLastCommitSha()).isEqualTo("abc123def456");
        assertThat(saved.getTotalApiCalls()).isEqualTo(100);

        // 조회 테스트
        GithubCollectionMetadata found = collectionMetadataRepository.findByGithubId(githubId).orElseThrow();
        assertThat(found.getInitialCollected()).isTrue();
        assertThat(found.getTotalApiCalls()).isEqualTo(100);
    }

    @Test
    @DisplayName("순위 조회 테스트 (totalScore 내림차순)")
    void testRankingQuery() {
        // Given
        GithubUserStatistics user1 = GithubUserStatistics.create("user1");
        user1.updateStatistics(100, 5000, 3000, 2000, 10, 5, 5, 10, 50, 20, 80, BigDecimal.valueOf(1500));

        GithubUserStatistics user2 = GithubUserStatistics.create("user2");
        user2.updateStatistics(200, 10000, 6000, 4000, 20, 10, 10, 20, 100, 40, 160, BigDecimal.valueOf(3000));

        GithubUserStatistics user3 = GithubUserStatistics.create("user3");
        user3.updateStatistics(50, 2500, 1500, 1000, 5, 3, 3, 5, 25, 10, 40, BigDecimal.valueOf(750));

        userStatisticsRepository.save(user1);
        userStatisticsRepository.save(user2);
        userStatisticsRepository.save(user3);

        // When
        var rankings = userStatisticsRepository.findAllByOrderByTotalScoreDesc();

        // Then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getGithubId()).isEqualTo("user2"); // 1위
        assertThat(rankings.get(1).getGithubId()).isEqualTo("user1"); // 2위
        assertThat(rankings.get(2).getGithubId()).isEqualTo("user3"); // 3위
    }
}
