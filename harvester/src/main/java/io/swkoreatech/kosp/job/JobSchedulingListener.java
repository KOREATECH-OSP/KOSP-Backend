package io.swkoreatech.kosp.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.client.RateLimitException;
import io.swkoreatech.kosp.queue.CollectionPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 잡 완료 후 다음 실행을 스케줄링하는 리스너.
 *
 * <p>잡 성공 시 다음 고정 슬롯(하루 3회)으로 예약하고,
 * 실패 시 에러 유형에 따라 재시도를 스케줄링한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulingListener implements JobExecutionListener {

    private final CollectionPublisher collectionPublisher;

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

        if (status == BatchStatus.COMPLETED) {
            collectionPublisher.scheduleNextSlot(userId);
            return;
        }

        if (isRateLimitError(jobExecution)) {
            collectionPublisher.scheduleRetry(userId, Instant.now().plus(30, ChronoUnit.MINUTES));
            return;
        }

        collectionPublisher.scheduleRetry(userId, Instant.now().plus(30, ChronoUnit.MINUTES));
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
}
