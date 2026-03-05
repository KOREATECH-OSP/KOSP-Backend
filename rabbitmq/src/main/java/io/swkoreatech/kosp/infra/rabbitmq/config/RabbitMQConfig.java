package io.swkoreatech.kosp.infra.rabbitmq.config;

import static io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames.*;

import java.util.HashMap;
import java.util.Map;

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

/**
 * RabbitMQ 인프라 설정 클래스.
 *
 * <p>메시지 변환기, RabbitTemplate, 큐, 익스체인지, 바인딩 등
 * RabbitMQ에 필요한 모든 빈을 등록한다.</p>
 *
 * <p>{@code spring.rabbitmq.host} 프로퍼티가 설정되어 있고
 * {@link ConnectionFactory} 클래스가 클래스패스에 존재할 때만 활성화된다.</p>
 */
@Slf4j
@Configuration
@EnableRabbit
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "host")
public class RabbitMQConfig {

    /**
     * Jackson 기반 JSON 메시지 변환기를 생성한다.
     *
     * <p>{@link JavaTimeModule}을 등록하여 Java 8 날짜/시간 타입을 ISO-8601 형식으로 직렬화한다.</p>
     *
     * @return JSON 직렬화/역직렬화를 수행하는 {@link MessageConverter}
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new Jackson2JsonMessageConverter(mapper);
    }

    /**
     * 메시지 발행에 사용되는 {@link RabbitTemplate}을 생성한다.
     *
     * <p>JSON 메시지 변환기를 적용하며, 발행 실패 시 에러 로그를 기록하는
     * confirm 콜백을 설정한다.</p>
     *
     * @param connectionFactory RabbitMQ 연결 팩토리
     * @param messageConverter  메시지 직렬화에 사용할 변환기
     * @return 설정이 완료된 {@link RabbitTemplate}
     */
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

    /**
     * 챌린지 평가 큐를 생성한다.
     *
     * <p>처리 실패 메시지는 데드 레터 큐({@code challenge-evaluation-queue.dlq})로 라우팅된다.</p>
     *
     * @return 데드 레터 설정이 적용된 내구성(durable) 큐
     */
    @Bean
    public Queue challengeEvaluationQueue() {
        return QueueBuilder.durable(CHALLENGE_EVALUATION)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "challenge-evaluation-queue.dlq")
            .build();
    }

    /**
     * 챌린지 완료 큐를 생성한다.
     *
     * <p>처리 실패 메시지는 데드 레터 큐({@code challenge-completed-queue.dlq})로 라우팅된다.</p>
     *
     * @return 데드 레터 설정이 적용된 내구성(durable) 큐
     */
    @Bean
    public Queue challengeCompletedQueue() {
        return QueueBuilder.durable(CHALLENGE_COMPLETED)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "challenge-completed-queue.dlq")
            .build();
    }

    /**
     * 포인트 변경 큐를 생성한다.
     *
     * <p>처리 실패 메시지는 데드 레터 큐({@code point-changed-queue.dlq})로 라우팅된다.</p>
     *
     * @return 데드 레터 설정이 적용된 내구성(durable) 큐
     */
    @Bean
    public Queue pointChangedQueue() {
        return QueueBuilder.durable(POINT_CHANGED)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "point-changed-queue.dlq")
            .build();
    }

    /**
     * 챌린지 평가 데드 레터 큐(DLQ)를 생성한다.
     *
     * <p>챌린지 평가 큐에서 처리에 실패한 메시지가 이 큐로 이동된다.</p>
     *
     * @return 내구성(durable) 데드 레터 큐
     */
    @Bean
    public Queue challengeEvaluationDLQ() {
        return QueueBuilder.durable("challenge-evaluation-queue.dlq").build();
    }

    /**
     * 챌린지 완료 데드 레터 큐(DLQ)를 생성한다.
     *
     * <p>챌린지 완료 큐에서 처리에 실패한 메시지가 이 큐로 이동된다.</p>
     *
     * @return 내구성(durable) 데드 레터 큐
     */
    @Bean
    public Queue challengeCompletedDLQ() {
        return QueueBuilder.durable("challenge-completed-queue.dlq").build();
    }

    /**
     * 포인트 변경 데드 레터 큐(DLQ)를 생성한다.
     *
     * <p>포인트 변경 큐에서 처리에 실패한 메시지가 이 큐로 이동된다.</p>
     *
     * @return 내구성(durable) 데드 레터 큐
     */
    @Bean
    public Queue pointChangedDLQ() {
        return QueueBuilder.durable("point-changed-queue.dlq").build();
    }

    /**
     * GitHub 활동 수집용 지연 메시지 익스체인지를 생성한다.
     *
     * <p>{@code x-delayed-message} 타입을 사용하여 메시지 발행 시
     * 지정된 지연 시간 후에 큐로 전달되도록 한다.</p>
     *
     * @return 지연 메시지를 지원하는 {@link CustomExchange}
     */
    @Bean
    public CustomExchange githubCollectionExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(GITHUB_COLLECTION_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * GitHub 활동 수집 큐를 생성한다.
     *
     * <p>처리 실패 메시지는 데드 레터 큐({@code github-collection-queue.dlq})로 라우팅된다.</p>
     *
     * @return 데드 레터 설정이 적용된 내구성(durable) 큐
     */
    @Bean
    public Queue githubCollectionQueue() {
        return QueueBuilder.durable(GITHUB_COLLECTION)
            .withArgument(X_DEAD_LETTER_EXCHANGE, "")
            .withArgument(X_DEAD_LETTER_ROUTING_KEY, "github-collection-queue.dlq")
            .build();
    }

    /**
     * GitHub 활동 수집 데드 레터 큐(DLQ)를 생성한다.
     *
     * <p>GitHub 활동 수집 큐에서 처리에 실패한 메시지가 이 큐로 이동된다.</p>
     *
     * @return 내구성(durable) 데드 레터 큐
     */
    @Bean
    public Queue githubCollectionDLQ() {
        return QueueBuilder.durable("github-collection-queue.dlq").build();
    }

    /**
     * GitHub 활동 수집 큐와 익스체인지를 바인딩한다.
     *
     * <p>라우팅 키로 {@link io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames#GITHUB_COLLECTION}을 사용한다.</p>
     *
     * @return 큐와 익스체인지 간의 {@link Binding}
     */
    @Bean
    public Binding githubCollectionBinding() {
        return BindingBuilder.bind(githubCollectionQueue())
            .to(githubCollectionExchange())
            .with(GITHUB_COLLECTION)
            .noargs();
    }
}
