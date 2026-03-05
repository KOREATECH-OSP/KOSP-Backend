package io.swkoreatech.kosp.launcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 우선순위 기반 배치 작업 실행기.
 *
 * <p>단일 스레드 ExecutorService를 사용하여 GitHub 수집 작업을 순차적으로 실행한다.
 * 메시지 큐에서 수신된 요청을 비동기로 처리하며, 작업 간 순서를 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriorityJobLauncher {

    private final JobLauncher jobLauncher;
    private final @Lazy Job githubCollectionJob;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 지정된 사용자에 대한 GitHub 수집 작업을 비동기로 실행한다.
     *
     * @param userId 작업을 실행할 사용자 ID
     * @param runId  실행 식별자
     */
    public void run(Long userId, String runId) {
        executor.submit(() -> executeJob(userId, runId));
    }

    private void executeJob(Long userId, String runId) {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("userId", userId, false)
                .addString("runId", runId, true)
                .toJobParameters();

            log.info("Launching job for user {} (runId: {})", userId, runId);
            jobLauncher.run(githubCollectionJob, params);
        } catch (Exception e) {
            log.error("Failed to launch job for user {}", userId, e);
        }
    }

    /**
     * 애플리케이션 종료 시 ExecutorService를 정상적으로 종료한다.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
