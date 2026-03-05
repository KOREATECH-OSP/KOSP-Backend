package io.swkoreatech.kosp.statistics.service;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.common.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.statistics.model.PlatformStatistics;
import io.swkoreatech.kosp.statistics.repository.PlatformStatisticsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 플랫폼 전체 사용자의 평균 통계를 계산하는 서비스.
 *
 * <p>모든 사용자의 GitHub 통계(커밋, 스타, PR, 이슈)를 집계하여
 * 평균값을 계산하고 플랫폼 통계에 저장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAverageCalculator {

    private static final String GLOBAL_STAT_KEY = "global";
    private static final int SCALE = 2;

    private final GithubUserStatisticsRepository userStatsRepository;
    private final PlatformStatisticsRepository platformStatsRepository;

    /**
     * 플랫폼 평균 통계를 계산하고 저장한다.
     *
     * <p>사용자 통계가 없는 경우 계산을 건너뛴다.
     */
    @Transactional
    public void calculateAndSave() {
        List<GithubUserStatistics> allUserStats = userStatsRepository.findAll();

        if (allUserStats.isEmpty()) {
            log.info("No user statistics found, skipping platform average calculation");
            return;
        }

        AverageResult averages = calculateAverages(allUserStats);
        saveAverages(averages, allUserStats.size());

        log.info("Platform averages calculated: commits={}, stars={}, prs={}, issues={} (from {} users)",
            averages.avgCommits, averages.avgStars, averages.avgPrs, averages.avgIssues, allUserStats.size());
    }

    private AverageResult calculateAverages(List<GithubUserStatistics> stats) {
        long totalCommits = 0;
        long totalStars = 0;
        long totalPrs = 0;
        long totalIssues = 0;

        for (GithubUserStatistics stat : stats) {
            totalCommits += stat.getTotalCommits();
            totalStars += stat.getTotalStarsReceived();
            totalPrs += stat.getTotalPrs();
            totalIssues += stat.getTotalIssues();
        }

        int count = stats.size();
        return new AverageResult(
            divide(totalCommits, count),
            divide(totalStars, count),
            divide(totalPrs, count),
            divide(totalIssues, count)
        );
    }

    private BigDecimal divide(long total, int count) {
        return BigDecimal.valueOf(total)
            .divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP);
    }

    private void saveAverages(AverageResult averages, int userCount) {
        PlatformStatistics platformStats = platformStatsRepository.getOrCreate(GLOBAL_STAT_KEY);

        platformStats.updateAverages(
            averages.avgCommits,
            averages.avgStars,
            averages.avgPrs,
            averages.avgIssues,
            userCount
        );

        platformStatsRepository.save(platformStats);
    }

    /**
     * 평균 계산 결과를 담는 레코드.
     *
     * @param avgCommits 평균 커밋 수
     * @param avgStars   평균 스타 수
     * @param avgPrs     평균 PR 수
     * @param avgIssues  평균 이슈 수
     */
    private record AverageResult(
        BigDecimal avgCommits,
        BigDecimal avgStars,
        BigDecimal avgPrs,
        BigDecimal avgIssues
    ) {}
}
