package io.swkoreatech.kosp.job;

import java.time.Duration;
import java.time.Instant;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.launcher.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulingListener implements JobExecutionListener {

    private static final Duration FAILURE_RETRY_DELAY = Duration.ofMinutes(30);
    private static final Duration BUFFER_AFTER_RESET = Duration.ofMinutes(5);

    private final TaskScheduler taskScheduler;
    private final PriorityJobLauncher priorityJobLauncher;
    private final RateLimitManager rateLimitManager;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        log.info("========== [User {}] JOB STARTED ==========", userId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        BatchStatus status = jobExecution.getStatus();

        log.info("========== [User {}] JOB FINISHED - {} ==========", userId, status);

        Instant nextRun = resolveNextRunTime(status);

        log.info("[User {}] Next run scheduled at {}", userId, nextRun);

        taskScheduler.schedule(
            () -> priorityJobLauncher.submit(userId, Priority.LOW),
            nextRun
        );
    }

    private Instant resolveNextRunTime(BatchStatus status) {
        if (status != BatchStatus.COMPLETED) {
            return Instant.now().plus(FAILURE_RETRY_DELAY);
        }
        return rateLimitManager.getResetTime().plus(BUFFER_AFTER_RESET);
    }
}
