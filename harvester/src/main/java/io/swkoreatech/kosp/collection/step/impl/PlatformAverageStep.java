package io.swkoreatech.kosp.collection.step.impl;

import java.math.BigDecimal;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.statistics.model.PlatformStatistics;
import io.swkoreatech.kosp.statistics.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.statistics.repository.PlatformStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformAverageStep implements StepProvider {

    private static final String STEP_NAME = "platformAverageStep";
    private static final String STAT_KEY = "GLOBAL";
    private static final int RECOMPUTE_THRESHOLD = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final PlatformStatisticsRepository platformStatisticsRepository;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                execute();
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepCompletionListener)
            .build();
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private void execute() {
        if (!shouldRecompute()) {
            return;
        }

        updateAverages();
    }

    private boolean shouldRecompute() {
        long currentUserCount = statisticsRepository.count();
        PlatformStatistics platformStats = platformStatisticsRepository.getOrCreate(STAT_KEY);
        int lastCalculatedCount = platformStats.getTotalUserCount();

        int delta = (int) (currentUserCount - lastCalculatedCount);
        if (delta < RECOMPUTE_THRESHOLD) {
            log.info("Threshold not met (delta: {}), skipping", delta);
            return false;
        }

        return true;
    }

    private void updateAverages() {
        BigDecimal avgCommits = getAverageOrZero(statisticsRepository.findAverageCommits());
        BigDecimal avgPrs = getAverageOrZero(statisticsRepository.findAveragePrs());
        BigDecimal avgIssues = getAverageOrZero(statisticsRepository.findAverageIssues());
        BigDecimal avgStars = getAverageOrZero(statisticsRepository.findAverageStars());
        int totalUserCount = (int) statisticsRepository.count();

        PlatformStatistics platformStats = platformStatisticsRepository.getOrCreate(STAT_KEY);
        platformStats.updateAverages(avgCommits, avgStars, avgPrs, avgIssues, totalUserCount);
        platformStatisticsRepository.save(platformStats);

        logUpdatedAverages(avgCommits, avgPrs, avgIssues, avgStars, totalUserCount);
    }

    private BigDecimal getAverageOrZero(BigDecimal average) {
        if (average == null) {
            return BigDecimal.ZERO;
        }
        return average;
    }

    private void logUpdatedAverages(
        BigDecimal avgCommits,
        BigDecimal avgPrs,
        BigDecimal avgIssues,
        BigDecimal avgStars,
        int totalUserCount
    ) {
        log.info("Updated platform averages: commits={}, prs={}, issues={}, stars={}, totalUsers={}",
            avgCommits, avgPrs, avgIssues, avgStars, totalUserCount);
    }
}
