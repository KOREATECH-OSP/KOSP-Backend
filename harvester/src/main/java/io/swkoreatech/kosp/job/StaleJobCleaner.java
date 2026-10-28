package io.swkoreatech.kosp.job;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 시작 시 이전 컨테이너 종료로 인해 STARTED 상태로 남은 좀비 잡을 정리한다.
 *
 * <p>컨테이너가 강제 종료되면 실행 중이던 Spring Batch 잡이 FAILED 처리 없이 죽어
 * DB에 STARTED 상태로 남는다. 이를 정리하지 않으면 isJobRunningForUser 체크가
 * 해당 유저를 "실행 중"으로 판단해 새 수집 요청을 버린다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaleJobCleaner implements ApplicationRunner {

    private static final String JOB_NAME = "githubCollectionJob";

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;

    @Override
    public void run(ApplicationArguments args) {
        Set<JobExecution> staleExecutions = jobExplorer.findRunningJobExecutions(JOB_NAME);

        if (staleExecutions.isEmpty()) {
            return;
        }

        log.warn("Found {} stale job execution(s) from previous container — marking as FAILED",
            staleExecutions.size());

        for (JobExecution execution : staleExecutions) {
            Long userId = execution.getJobParameters().getLong("userId");
            log.warn("Cleaning up stale job execution: id={}, userId={}, startTime={}",
                execution.getId(), userId, execution.getStartTime());

            execution.setStatus(BatchStatus.FAILED);
            execution.setExitStatus(new ExitStatus("FAILED", "Cleaned up on startup — container was restarted"));
            execution.setEndTime(LocalDateTime.now());
            jobRepository.update(execution);
        }

        log.info("Stale job cleanup complete: {} execution(s) marked as FAILED", staleExecutions.size());
    }
}
