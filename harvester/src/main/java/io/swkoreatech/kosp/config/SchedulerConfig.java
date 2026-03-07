package io.swkoreatech.kosp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 스케줄러 설정 클래스.
 *
 * <p>Spring 스케줄링을 활성화하고, 스레드 풀 기반의 TaskScheduler를 구성한다.
 * 정기적인 플랫폼 평균 계산 등의 예약 작업에 사용된다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * 스레드 풀 기반 TaskScheduler 빈을 생성한다.
     *
     * @return 구성된 TaskScheduler 인스턴스
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("harvester-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
