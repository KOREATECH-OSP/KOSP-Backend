package kr.ac.koreatech.sw.kosp.domain.github.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubSyncScheduler {

    private final JobLauncher jobLauncher;
    private final Job githubSyncJob;

    // 매일 자정 실행 (0 0 0 * * *)
    @Scheduled(cron = "0 0 0 * * *")
    public void runGithubSyncJob() {
        log.info("Starting Github Sync Job...");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            
            jobLauncher.run(githubSyncJob, jobParameters);
            log.info("Github Sync Job finished successfully.");
        } catch (Exception e) {
            log.error("Github Sync Job failed", e);
        }
    }
}
