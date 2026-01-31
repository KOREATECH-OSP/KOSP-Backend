package io.swkoreatech.kosp.infra.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiServiceRabbitMQConfig {
    
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
