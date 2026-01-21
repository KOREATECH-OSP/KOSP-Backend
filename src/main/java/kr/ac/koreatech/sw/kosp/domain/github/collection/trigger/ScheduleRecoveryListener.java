package kr.ac.koreatech.sw.kosp.domain.github.collection.trigger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.JobPriority;
import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.PriorityJobLauncher;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleRecoveryListener {

    private static final String JOB_NAME = "githubCollectionJob";
    private static final Duration DEFAULT_INTERVAL = Duration.ofHours(2);
    private static final int MAX_INSTANCES = 100;

    private final JobExplorer jobExplorer;
    private final TaskScheduler taskScheduler;
    private final PriorityJobLauncher priorityJobLauncher;
    private final GithubUserRepository githubUserRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverSchedules() {
        log.info("Recovering schedules on application startup...");

        List<Long> activeUserIds = githubUserRepository.findAllGithubIds();
        int scheduled = 0;
        int immediate = 0;

        for (Long userId : activeUserIds) {
            Instant lastRun = getLastExecutionTime(userId);
            Instant nextRun = calculateNextRun(lastRun);

            if (nextRun.isBefore(Instant.now())) {
                priorityJobLauncher.submit(userId, JobPriority.LOW);
                immediate++;
            } else {
                scheduleNextRun(userId, nextRun);
                scheduled++;
            }
        }

        log.info("Schedule recovery complete. Immediate: {}, Scheduled: {}", immediate, scheduled);
    }

    private Instant getLastExecutionTime(Long userId) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(JOB_NAME, 0, MAX_INSTANCES);

        return instances.stream()
            .map(jobExplorer::getLastJobExecution)
            .filter(Objects::nonNull)
            .filter(exec -> userId.equals(exec.getJobParameters().getLong("userId")))
            .map(JobExecution::getEndTime)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .map(this::toInstant)
            .orElse(null);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private Instant calculateNextRun(Instant lastRun) {
        if (lastRun == null) {
            return Instant.now();
        }
        return lastRun.plus(DEFAULT_INTERVAL);
    }

    private void scheduleNextRun(Long userId, Instant nextRun) {
        taskScheduler.schedule(
            () -> priorityJobLauncher.submit(userId, JobPriority.LOW),
            nextRun
        );
    }
}
