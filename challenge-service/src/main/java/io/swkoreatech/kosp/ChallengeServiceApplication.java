package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    scanBasePackages = {
        "io.swkoreatech.kosp.challenge",
        "io.swkoreatech.kosp.common",
        "io.swkoreatech.kosp.infra.rabbitmq"
    }
)
@EnableScheduling
public class ChallengeServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChallengeServiceApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
