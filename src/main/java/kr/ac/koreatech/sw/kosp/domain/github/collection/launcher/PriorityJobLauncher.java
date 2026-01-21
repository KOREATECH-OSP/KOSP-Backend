package kr.ac.koreatech.sw.kosp.domain.github.collection.launcher;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriorityJobLauncher {

    private static final String JOB_NAME = "githubCollectionJob";
    private static final int MAX_INSTANCES_TO_CHECK = 10;

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Job githubCollectionJob;

    private final PriorityBlockingQueue<JobLaunchRequest> queue = new PriorityBlockingQueue<>();

    public void submit(Long userId, JobPriority priority) {
        queue.offer(JobLaunchRequest.of(userId, priority));
        log.info("Submitted job request for user {} with priority {}", userId, priority);
    }

    @Async("jobLauncherExecutor")
    @Scheduled(fixedDelayString = "${github.collection.launcher.poll-interval:1000}")
    public void processQueue() {
        JobLaunchRequest request = queue.poll();
        if (request == null) {
            return;
        }

        try {
            launchJob(request);
        } catch (Exception e) {
            log.error("Failed to launch job for user {}: {}", request.userId(), e.getMessage(), e);
        }
    }

    private void launchJob(JobLaunchRequest request) throws Exception {
        Long userId = request.userId();

        if (isJobRunning(userId)) {
            log.info("Job already running for user {}, skipping", userId);
            return;
        }

        JobExecution failedExecution = findLastFailedExecution(userId);

        if (failedExecution != null) {
            restartFailedJob(failedExecution, userId);
            return;
        }

        startNewJob(userId);
    }

    private boolean isJobRunning(Long userId) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(JOB_NAME, 0, MAX_INSTANCES_TO_CHECK);

        for (JobInstance instance : instances) {
            JobExecution lastExec = jobExplorer.getLastJobExecution(instance);
            if (lastExec == null) {
                continue;
            }
            if (!lastExec.isRunning()) {
                continue;
            }
            Long execUserId = lastExec.getJobParameters().getLong("userId");
            if (Objects.equals(execUserId, userId)) {
                return true;
            }
        }
        return false;
    }

    private JobExecution findLastFailedExecution(Long userId) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(JOB_NAME, 0, MAX_INSTANCES_TO_CHECK);

        for (JobInstance instance : instances) {
            JobExecution lastExec = jobExplorer.getLastJobExecution(instance);
            if (lastExec == null) {
                continue;
            }
            Long execUserId = lastExec.getJobParameters().getLong("userId");
            if (!Objects.equals(execUserId, userId)) {
                continue;
            }
            if (lastExec.getStatus() == BatchStatus.FAILED) {
                return lastExec;
            }
        }
        return null;
    }

    private void restartFailedJob(JobExecution failedExecution, Long userId) throws Exception {
        log.info("Restarting failed job execution {} for user {}", failedExecution.getId(), userId);
        jobOperator.restart(failedExecution.getId());
    }

    private void startNewJob(Long userId) throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("userId", userId, true)
            .addLong("scheduledAt", Instant.now().toEpochMilli(), true)
            .toJobParameters();

        log.info("Starting new job for user {} with scheduledAt {}", userId, params.getLong("scheduledAt"));
        jobLauncher.run(githubCollectionJob, params);
    }
}
