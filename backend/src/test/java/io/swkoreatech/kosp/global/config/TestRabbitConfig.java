package io.swkoreatech.kosp.global.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestRabbitConfig {

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }
}
