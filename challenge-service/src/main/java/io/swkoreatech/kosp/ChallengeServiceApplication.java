package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "io.swkoreatech.kosp.challenge",
    "io.swkoreatech.kosp.domain.challenge",
    "io.swkoreatech.kosp.domain.user",
    "io.swkoreatech.kosp.domain.github",
    "io.swkoreatech.kosp.domain.point",
    "io.swkoreatech.kosp.infra.rabbitmq"
})
@EnableJpaRepositories(basePackages = {
    "io.swkoreatech.kosp.domain.challenge.repository",
    "io.swkoreatech.kosp.domain.user.repository",
    "io.swkoreatech.kosp.domain.github.repository"
})
@EntityScan(basePackages = {
    "io.swkoreatech.kosp.domain.challenge.model",
    "io.swkoreatech.kosp.domain.user.model",
    "io.swkoreatech.kosp.common.github.model",
    "io.swkoreatech.kosp.common.model"
})
@EnableScheduling
public class ChallengeServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ChallengeServiceApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
