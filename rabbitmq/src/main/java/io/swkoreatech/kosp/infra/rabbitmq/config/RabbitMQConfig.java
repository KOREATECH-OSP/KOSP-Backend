package io.swkoreatech.kosp.infra.rabbitmq.config;

import static io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames.*;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

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
        MessageConverter messageConverter
    ) {
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
        return QueueBuilder.durable(CHALLENGE_EVALUATION)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "challenge-evaluation-queue.dlq")
            .build();
    }

    @Bean
    public Queue challengeCompletedQueue() {
        return QueueBuilder.durable(CHALLENGE_COMPLETED)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "challenge-completed-queue.dlq")
            .build();
    }

    @Bean
    public Queue pointChangedQueue() {
        return QueueBuilder.durable(POINT_CHANGED)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "point-changed-queue.dlq")
            .build();
    }

    @Bean
    public Queue challengeEvaluationDLQ() {
        return QueueBuilder.durable("challenge-evaluation-queue.dlq").build();
    }

    @Bean
    public Queue challengeCompletedDLQ() {
        return QueueBuilder.durable("challenge-completed-queue.dlq").build();
    }

    @Bean
    public Queue pointChangedDLQ() {
        return QueueBuilder.durable("point-changed-queue.dlq").build();
    }

    @Bean
    public CustomExchange githubCollectionExchange() {
        // TODO: rabbitmq_delayed_message_exchange plugin is deprecated in RabbitMQ 4.3+
        // (Mnesia-based, will be removed with Mnesia). Migrate to TTL+DLX pattern before
        // upgrading to RabbitMQ 4.3+. See: https://github.com/rabbitmq/rabbitmq-delayed-message-exchange
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(GITHUB_COLLECTION_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue githubCollectionQueue() {
        return QueueBuilder.durable(GITHUB_COLLECTION)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "github-collection-queue.dlq")
            .build();
    }

    @Bean
    public Queue githubCollectionDLQ() {
        return QueueBuilder.durable("github-collection-queue.dlq").build();
    }

    @Bean
    public Binding githubCollectionBinding() {
        return BindingBuilder.bind(githubCollectionQueue())
            .to(githubCollectionExchange())
            .with(GITHUB_COLLECTION)
            .noargs();
    }
}
