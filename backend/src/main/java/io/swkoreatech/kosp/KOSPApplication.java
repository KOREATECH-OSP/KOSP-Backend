package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * KOSP 백엔드 애플리케이션 메인 클래스.
 */
@ConfigurationPropertiesScan
@SpringBootApplication
public class KOSPApplication {

    /** 애플리케이션을 시작한다. */
    public static void main(String[] args) {
        SpringApplication.run(KOSPApplication.class, args);
    }

}
