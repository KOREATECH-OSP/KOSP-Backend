package io.swkoreatech.kosp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 작업 실행을 위한 설정 클래스.
 *
 * <p>Spring Batch 및 비동기 처리에 사용될 스레드 풀 기반의 TaskExecutor를 구성한다.
 */
@Configuration
public class AsyncConfig {

    /**
     * 스레드 풀 기반 TaskExecutor 빈을 생성한다.
     *
     * @return 구성된 TaskExecutor 인스턴스
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("harvester-async-");
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
