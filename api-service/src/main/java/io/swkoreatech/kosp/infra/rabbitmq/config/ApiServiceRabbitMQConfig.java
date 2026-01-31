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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableRabbit
public class ApiServiceRabbitMQConfig {
    
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
    public Queue jobQueuePublisherQueue() {
        return QueueBuilder.durable("job-queue-publisher-queue")
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", "job-queue-publisher-queue.dlq")
            .build();
    }
    
    @Bean
    public Queue jobQueuePublisherDLQ() {
        return QueueBuilder.durable("job-queue-publisher-queue.dlq").build();
    }
}
