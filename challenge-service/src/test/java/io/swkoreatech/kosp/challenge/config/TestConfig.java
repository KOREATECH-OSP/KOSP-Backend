package io.swkoreatech.kosp.challenge.config;

import io.swkoreatech.kosp.challenge.publisher.ChallengeEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public ChallengeEventPublisher challengeEventPublisher(RabbitTemplate rabbitTemplate) {
        return new ChallengeEventPublisher(rabbitTemplate);
    }
}
