package io.swkoreatech.kosp.domain.user.eventlistener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final StringRedisTemplate redisTemplate;

    @Value("${harvester.redis.collection-trigger-stream:github:collection:trigger}")
    private String streamKey;

    @EventListener
    public void handleUserSignup(UserSignupEvent event) {
        log.info("Received UserSignupEvent for user {} (GitHub: {})", 
            event.getUserId(), event.getGithubLogin());

        publishCollectionTrigger(event.getUserId());
    }

    private void publishCollectionTrigger(Long userId) {
        Map<String, String> payload = Map.of(
            "userId", String.valueOf(userId)
        );

        var record = StreamRecords.string(payload).withStreamKey(streamKey);
        redisTemplate.opsForStream().add(record);

        log.info("Published collection trigger to Harvester for user {}", userId);
    }
}
