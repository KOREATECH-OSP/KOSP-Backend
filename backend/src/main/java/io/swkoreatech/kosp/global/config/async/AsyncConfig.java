package io.swkoreatech.kosp.global.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정 클래스.
 * <p>Spring의 {@code @Async} 어노테이션 기반 비동기 메서드 실행을 활성화한다.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

}
