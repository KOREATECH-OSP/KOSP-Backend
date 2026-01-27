package io.swkoreatech.kosp.queue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.swkoreatech.kosp.common.queue.JobQueueEntry;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.queue.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJobQueueListener {
    private final JobQueueService jobQueueService;
    private final PriorityJobLauncher jobLauncher;
    private final UserRepository userRepository;
    private final JobExplorer jobExplorer;

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        Optional<JobQueueEntry> entry = jobQueueService.dequeue();
        if (entry.isEmpty()) {
            return;
        }
        processEntry(entry.get());
    }

    private void processEntry(JobQueueEntry entry) {
        if (isUserDeleted(entry.userId())) {
            log.info("Skipping job for deleted user: {}", entry.userId());
            return;
        }
        if (isJobRunningForUser(entry.userId())) {
            log.info("Job already running for user {}, re-queuing with 1 min delay", entry.userId());
            jobQueueService.enqueue(
                entry.userId(),
                entry.runId(),
                Instant.now().plus(1, ChronoUnit.MINUTES),
                Priority.HIGH
            );
            return;
        }
        jobLauncher.run(entry.userId(), entry.runId());
    }

    private boolean isUserDeleted(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return true;
        }
        return user.isDeleted();
    }

    private boolean isJobRunningForUser(Long userId) {
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("githubCollectionJob");
        return runningExecutions.stream()
            .anyMatch(execution -> {
                Long jobUserId = execution.getJobParameters().getLong("userId");
                return userId.equals(jobUserId);
            });
    }
}
