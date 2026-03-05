package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Harvester 모듈의 Spring Boot 애플리케이션 진입점.
 *
 * <p>GitHub 데이터 수집 및 분석을 위한 배치 처리 애플리케이션을 시작한다.
 */
@ConfigurationPropertiesScan
@SpringBootApplication
public class HarvesterApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(HarvesterApplication.class, args);
    }
}
