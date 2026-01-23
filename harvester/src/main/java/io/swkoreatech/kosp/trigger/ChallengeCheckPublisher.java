package io.swkoreatech.kosp.trigger;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCheckPublisher {

    private final StringRedisTemplate redisTemplate;

    @Value("${harvester.redis.challenge-check-stream:kosp:challenge-check}")
    private String streamKey;

    public void publish(Long userId, String githubId) {
        Map<String, String> payload = Map.of(
            "userId", String.valueOf(userId),
            "githubId", githubId,
            "timestamp", String.valueOf(System.currentTimeMillis())
        );

        var record = StreamRecords.string(payload).withStreamKey(streamKey);
        redisTemplate.opsForStream().add(record);

        log.info("Published challenge check request for user {} (githubId: {})", userId, githubId);
    }
}
