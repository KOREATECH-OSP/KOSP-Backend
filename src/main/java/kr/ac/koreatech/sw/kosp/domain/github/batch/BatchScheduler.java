package kr.ac.koreatech.sw.kosp.domain.github.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job dataCollectionJob;
    private final Job statisticsCalculationJob;
    
    /**
     * 1시간마다 데이터 수집 및 통계 계산
     */
    @Scheduled(cron = "0 0 * * * *")
    public void runDataCollectionAndStatistics() {
        try {
            long timestamp = System.currentTimeMillis();
            
            // 1. 데이터 수집
            JobParameters collectionParams = new JobParametersBuilder()
                .addLong("timestamp", timestamp)
                .toJobParameters();
            
            jobLauncher.run(dataCollectionJob, collectionParams);
            log.info("Data collection job completed successfully");
            
            // 2. 통계 계산 (수집 직후)
            JobParameters statsParams = new JobParametersBuilder()
                .addLong("timestamp", timestamp + 1)  // 다른 timestamp로 구분
                .toJobParameters();
            
            jobLauncher.run(statisticsCalculationJob, statsParams);
            log.info("Statistics calculation job completed successfully");
            
        } catch (Exception e) {
            log.error("Batch job failed", e);
        }
    }
}
