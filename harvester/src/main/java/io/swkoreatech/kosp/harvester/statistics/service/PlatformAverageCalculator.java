package io.swkoreatech.kosp.harvester.statistics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.harvester.statistics.model.PlatformStatistics;
import io.swkoreatech.kosp.harvester.statistics.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.harvester.statistics.repository.PlatformStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformAverageCalculator {

    private static final String GLOBAL_STAT_KEY = "global";
    private static final int SCALE = 2;

    private final GithubUserStatisticsRepository userStatsRepository;
    private final PlatformStatisticsRepository platformStatsRepository;

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

    private record AverageResult(
        BigDecimal avgCommits,
        BigDecimal avgStars,
        BigDecimal avgPrs,
        BigDecimal avgIssues
    ) {}
}
