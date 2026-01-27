package io.swkoreatech.kosp.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.client.RateLimitException;
import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.queue.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulingListener implements JobExecutionListener {

    private final JobQueueService jobQueueService;
    private final RateLimitManager rateLimitManager;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        log.info("========== [User {}] JOB STARTED ==========", userId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        String runId = jobExecution.getJobParameters().getString("runId");
        BatchStatus status = jobExecution.getStatus();
        log.info("========== [User {}] JOB FINISHED - {} ==========", userId, status);

        if (status == BatchStatus.COMPLETED) {
            scheduleNextRun(userId);
            return;
        }

        if (isRateLimitError(jobExecution)) {
            scheduleRetry(userId, runId, getResetTimePlus5Min());
            return;
        }

        scheduleRetry(userId, runId, Instant.now().plus(30, ChronoUnit.MINUTES));
    }

    private boolean isRateLimitError(JobExecution execution) {
        List<Throwable> exceptions = execution.getAllFailureExceptions();
        return exceptions.stream()
            .anyMatch(this::containsRateLimitException);
    }

    private boolean containsRateLimitException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (throwable instanceof RateLimitException) {
            return true;
        }
        return containsRateLimitException(throwable.getCause());
    }

    private void scheduleNextRun(Long userId) {
        String newRunId = UUID.randomUUID().toString();
        Instant nextRun = getResetTimePlus5Min();
        jobQueueService.enqueue(userId, newRunId, nextRun, Priority.LOW);
        log.info("Scheduled next run for user {} at {}", userId, nextRun);
    }

    private void scheduleRetry(Long userId, String runId, Instant scheduledAt) {
        jobQueueService.enqueue(userId, runId, scheduledAt, Priority.HIGH);
        log.info("Scheduled retry for user {} at {}", userId, scheduledAt);
    }

    private Instant getResetTimePlus5Min() {
        return rateLimitManager.getResetTime().plus(5, ChronoUnit.MINUTES);
    }
}
