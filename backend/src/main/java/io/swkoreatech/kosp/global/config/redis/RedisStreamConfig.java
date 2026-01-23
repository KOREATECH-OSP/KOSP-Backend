package io.swkoreatech.kosp.global.config.redis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import io.swkoreatech.kosp.domain.challenge.listener.ChallengeCheckListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    private static final String STREAM_KEY = "kosp:challenge-check";
    private static final String CONSUMER_GROUP = "backend-challenge-group";
    private static final String CONSUMER_NAME = "backend-consumer-1";

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public Subscription challengeCheckSubscription(
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
        ChallengeCheckListener listener
    ) {
        createConsumerGroupIfNotExists();

        Subscription subscription = container.receive(
            Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
            StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
            listener
        );

        container.start();
        log.info("Redis Stream subscription started for stream: {}", STREAM_KEY);

        return subscription;
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofSeconds(1))
                .build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    private void createConsumerGroupIfNotExists() {
        try {
            redisConnectionFactory.getConnection().streamCommands()
                .xGroupCreate(STREAM_KEY.getBytes(), CONSUMER_GROUP, ReadOffset.from("0"), true);
            log.info("Created consumer group: {} for stream: {}", CONSUMER_GROUP, STREAM_KEY);
        } catch (Exception e) {
            log.debug("Consumer group already exists or stream not ready: {}", e.getMessage());
        }
    }
}
