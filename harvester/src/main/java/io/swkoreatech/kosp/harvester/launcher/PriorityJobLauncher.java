package io.swkoreatech.kosp.harvester.launcher;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PriorityJobLauncher {
    
    private static final String JOB_NAME = "githubCollectionJob";
    
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job githubCollectionJob;
    
    private final PriorityBlockingQueue<JobLaunchRequest> queue = new PriorityBlockingQueue<>();
    private final ExecutorService jobExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean jobRunning = new AtomicBoolean(false);
    
    public PriorityJobLauncher(
        JobLauncher jobLauncher,
        JobExplorer jobExplorer,
        @Lazy Job githubCollectionJob
    ) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.githubCollectionJob = githubCollectionJob;
    }
    
    public void submit(Long userId, Priority priority) {
        if (isJobRunning(userId)) {
            log.info("Job already running for user {}, skipping", userId);
            return;
        }
        
        if (isAlreadyQueued(userId)) {
            log.debug("Job already queued for user {}, skipping", userId);
            return;
        }
        
        queue.offer(new JobLaunchRequest(userId, priority, Instant.now()));
        log.info("Queued job for user {} with priority {}", userId, priority);
    }
    
    @Scheduled(fixedDelay = 100)
    public void processQueue() {
        if (jobRunning.get()) {
            return;
        }
        
        JobLaunchRequest request = queue.poll();
        if (request == null) {
            return;
        }
        
        if (isJobRunning(request.userId())) {
            log.debug("Job started by another thread for user {}, skipping", request.userId());
            return;
        }
        
        jobRunning.set(true);
        jobExecutor.submit(() -> executeJob(request));
    }
    
    private void executeJob(JobLaunchRequest request) {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("userId", request.userId())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            log.info("Launching job for user {} (priority: {})", request.userId(), request.priority());
            jobLauncher.run(githubCollectionJob, params);
        } catch (Exception exception) {
            log.error("Failed to launch job for user {}", request.userId(), exception);
        } finally {
            jobRunning.set(false);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        jobExecutor.shutdown();
    }
    
    private boolean isJobRunning(Long userId) {
        return jobExplorer.findRunningJobExecutions(JOB_NAME).stream()
            .anyMatch(exec -> userId.equals(exec.getJobParameters().getLong("userId")));
    }
    
    private boolean isAlreadyQueued(Long userId) {
        return queue.stream().anyMatch(req -> req.userId().equals(userId));
    }
    
    public int getQueueSize() {
        return queue.size();
    }
}
