package io.swkoreatech.kosp.job;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.client.RateLimitException;
import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulingListener implements JobExecutionListener {

    private final RabbitTemplate rabbitTemplate;
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
            scheduleRetry(userId, runId, getResetTimePlus5Min(userId));
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
        Instant nextRun = getResetTimePlus5Min(userId);
        int delayMs = (int) Math.max(0, Duration.between(Instant.now(), nextRun).toMillis());
        publishWithRetry(userId, delayMs);
        log.info("Scheduled next run for user {} at {}", userId, nextRun);
    }

    private void scheduleRetry(Long userId, String runId, Instant scheduledAt) {
        int delayMs = (int) Math.max(0, Duration.between(Instant.now(), scheduledAt).toMillis());
        publishWithRetry(userId, delayMs);
        log.info("Scheduled retry for user {} at {}", userId, scheduledAt);
    }

    private void publishWithRetry(Long userId, int delayMs) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                publish(userId, delayMs);
                return;
            } catch (Exception e) {
                handleRetryFailure(userId, attempt, e);
            }
        }
    }

    private void publish(Long userId, int delayMs) {
        GithubCollectionRequest dto = new GithubCollectionRequest(userId);
        rabbitTemplate.convertAndSend(
            QueueNames.GITHUB_COLLECTION_EXCHANGE,
            QueueNames.GITHUB_COLLECTION,
            dto,
            message -> {
                message.getMessageProperties().setHeader("x-delay", delayMs);
                return message;
            }
        );
    }

    private void handleRetryFailure(Long userId, int attempt, Exception e) {
        if (attempt == 3) {
            log.error("Failed to publish after 3 attempts for user {}", userId, e);
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Instant getResetTimePlus5Min(Long userId) {
        return rateLimitManager.getResetTime(userId).plus(5, ChronoUnit.MINUTES);
    }
}
