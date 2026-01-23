package io.swkoreatech.kosp.harvester.config;

import io.swkoreatech.kosp.harvester.trigger.CollectionTriggerListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Configuration
public class RedisStreamConfig {
    
    @Value("${harvester.redis.stream.key}")
    private String streamKey;
    
    @Value("${harvester.redis.stream.consumer-group}")
    private String consumerGroup;
    
    @Value("${harvester.redis.stream.consumer-name}")
    private String consumerName;
    
    private final StringRedisTemplate redisTemplate;
    
    public RedisStreamConfig(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @PostConstruct
    public void initializeConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, consumerGroup);
            log.info("Created consumer group '{}' for stream '{}'", consumerGroup, streamKey);
        } catch (Exception e) {
            handleConsumerGroupException(e);
        }
    }

    private void handleConsumerGroupException(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
            log.debug("Consumer group '{}' already exists", consumerGroup);
            return;
        }
        log.warn("Failed to create consumer group: {}", e.getMessage());
    }
    
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(
            RedisConnectionFactory connectionFactory,
            CollectionTriggerListener listener) {
        
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
            .<String, MapRecord<String, String, String>>builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build();
        
        var container = StreamMessageListenerContainer.create(connectionFactory, options);
        
        Subscription subscription = container.receiveAutoAck(
            Consumer.from(consumerGroup, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            listener
        );
        
        container.start();
        log.info("Started Redis Stream listener for '{}' as consumer '{}/{}'", 
            streamKey, consumerGroup, consumerName);
        
        return container;
    }
}
