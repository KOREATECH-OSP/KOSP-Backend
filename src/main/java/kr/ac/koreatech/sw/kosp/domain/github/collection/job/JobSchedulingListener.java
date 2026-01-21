package kr.ac.koreatech.sw.kosp.domain.github.collection.job;

import java.time.Duration;
import java.time.Instant;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.JobPriority;
import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulingListener implements JobExecutionListener {

    private static final Duration SUCCESS_INTERVAL = Duration.ofHours(2);
    private static final Duration FAILURE_INTERVAL = Duration.ofHours(1);

    private final TaskScheduler taskScheduler;
    private final PriorityJobLauncher priorityJobLauncher;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        log.info("Starting job for user {}", userId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long userId = jobExecution.getJobParameters().getLong("userId");
        BatchStatus status = jobExecution.getStatus();

        Duration interval = resolveNextInterval(status);
        Instant nextRun = Instant.now().plus(interval);

        log.info("Job completed for user {} with status {}. Next run scheduled at {}",
            userId, status, nextRun);

        taskScheduler.schedule(
            () -> priorityJobLauncher.submit(userId, JobPriority.LOW),
            nextRun
        );
    }

    private Duration resolveNextInterval(BatchStatus status) {
        if (status == BatchStatus.COMPLETED) {
            return SUCCESS_INTERVAL;
        }
        return FAILURE_INTERVAL;
    }
}
