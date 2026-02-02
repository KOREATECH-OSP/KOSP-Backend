package io.swkoreatech.kosp.infra.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableRabbit
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
public class RabbitMQConfig {
    
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message publish failed: {}", cause);
            }
        });
        
        return template;
    }
    
    @Bean
    public Queue challengeEvaluationQueue() {
        return QueueBuilder.durable("challenge-evaluation-queue")
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "challenge-evaluation-queue.dlq")
            .build();
    }
    
    @Bean
    public Queue pointChangedQueue() {
        return QueueBuilder.durable("point-changed-queue")
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "point-changed-queue.dlq")
            .build();
    }
    
    @Bean
    public Queue challengeEvaluationDLQ() {
        return QueueBuilder.durable("challenge-evaluation-queue.dlq").build();
    }
    
    @Bean
    public Queue pointChangedDLQ() {
        return QueueBuilder.durable("point-changed-queue.dlq").build();
    }
}
