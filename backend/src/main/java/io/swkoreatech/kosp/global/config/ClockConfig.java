package io.swkoreatech.kosp.global.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Clock} 빈 설정 클래스.
 * <p>시스템 기본 시간대를 사용하는 Clock 인스턴스를 빈으로 등록한다.
 * 테스트 시 고정 시간의 Clock으로 교체할 수 있다.</p>
 */
@Configuration
public class ClockConfig {

    /**
     * 시스템 기본 시간대의 Clock 빈을 생성한다.
     *
     * @return Clock 인스턴스
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
