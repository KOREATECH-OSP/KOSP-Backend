package io.swkoreatech.kosp.launcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriorityJobLauncher {
    
    private final JobLauncher jobLauncher;
    private final @Lazy Job githubCollectionJob;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public void run(Long userId, String runId) {
        executor.submit(() -> executeJob(userId, runId));
    }
    
    private void executeJob(Long userId, String runId) {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("userId", userId, false)
                .addString("runId", runId, true)
                .toJobParameters();
            
            log.info("Launching job for user {} (runId: {})", userId, runId);
            jobLauncher.run(githubCollectionJob, params);
        } catch (Exception e) {
            log.error("Failed to launch job for user {}", userId, e);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("PriorityJobLauncher shutting down, awaiting task completion");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate within 30s, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Shutdown interrupted, forcing executor termination");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
