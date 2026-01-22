package io.swkoreatech.kosp.harvester.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriorityJobLauncher {
    
    private static final String JOB_NAME = "githubCollectionJob";
    
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job githubCollectionJob;
    
    private final PriorityBlockingQueue<JobLaunchRequest> queue = new PriorityBlockingQueue<>();
    
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
        JobLaunchRequest request = queue.poll();
        if (request == null) {
            return;
        }
        
        if (isJobRunning(request.userId())) {
            log.debug("Job started by another thread for user {}, skipping", request.userId());
            return;
        }
        
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("userId", request.userId())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            log.info("Launching job for user {} (priority: {})", request.userId(), request.priority());
            jobLauncher.run(githubCollectionJob, params);
        } catch (Exception e) {
            log.error("Failed to launch job for user {}", request.userId(), e);
        }
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
