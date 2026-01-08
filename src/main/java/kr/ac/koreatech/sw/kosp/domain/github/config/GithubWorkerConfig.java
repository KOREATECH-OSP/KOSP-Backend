package kr.ac.koreatech.sw.kosp.domain.github.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync
public class GithubWorkerConfig {

    /**
     * GitHub Collection Worker용 Thread Pool
     * CPU 코어 수에 따라 동적으로 Worker 수 조정
     */
    @Bean(name = "githubWorkerExecutor")
    public Executor githubWorkerExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.max(1, processors / 2);  // 최소 1개, CPU/2 (1 core → 1 worker)
        int maxPoolSize = Math.max(2, processors);       // 최소 2개, 최대 CPU 코어 수
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("github-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("🚀 GitHub Worker Pool initialized: core={}, max={}, queue=100", 
            corePoolSize, maxPoolSize);
        log.info("💻 System info: {} CPU cores detected", processors);
        
        return executor;
    }
}
