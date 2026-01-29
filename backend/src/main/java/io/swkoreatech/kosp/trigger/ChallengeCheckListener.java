package io.swkoreatech.kosp.trigger;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.challenge.service.ChallengeEvaluator;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCheckListener {

    private static final String STREAM_KEY = "kosp:challenge-check";
    private static final String CONSUMER_GROUP = "backend-consumer-group";
    private static final String CONSUMER_NAME = "backend-consumer-1";
    private static final String PROCESSED_JOBS_KEY = "kosp:processed-jobs";

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final ChallengeEvaluator challengeEvaluator;

    @Scheduled(fixedDelay = 1000)
    public void pollMessages() {
        try {
            ensureConsumerGroupExists();
            readAndProcessMessages();
        } catch (Exception e) {
            log.error("Failed to poll challenge check events", e);
        }
    }

    private void ensureConsumerGroupExists() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, CONSUMER_GROUP);
            log.info("Created consumer group: {}", CONSUMER_GROUP);
        } catch (Exception e) {
            log.debug("Consumer group already exists or stream not ready");
        }
    }

    private void readAndProcessMessages() {
        List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
            org.springframework.data.redis.connection.stream.Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
            StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
        );

        if (messages == null) {
            return;
        }

        processMessages(messages);
    }

    private void processMessages(List<MapRecord<String, Object, Object>> messages) {
        for (MapRecord<String, Object, Object> message : messages) {
            processMessage(message);
            acknowledgeMessage(message);
        }
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        try {
            executeMessageProcessing(message);
        } catch (Exception e) {
            logProcessingError(message, e);
        }
    }

    private void executeMessageProcessing(MapRecord<String, Object, Object> message) {
        Map<Object, Object> payload = message.getValue();
        String jobExecutionId = String.valueOf(payload.get("jobExecutionId"));

        if (isAlreadyProcessed(jobExecutionId)) {
            log.debug("Skipping already processed jobExecutionId: {}", jobExecutionId);
            return;
        }

        evaluateChallenge(payload, jobExecutionId);
    }

    private boolean isAlreadyProcessed(String jobExecutionId) {
        Long result = redisTemplate.opsForSet().add(PROCESSED_JOBS_KEY, jobExecutionId);
        return result == null || result == 0;
    }

    private void evaluateChallenge(Map<Object, Object> payload, String jobExecutionId) {
        Long userId = Long.parseLong(String.valueOf(payload.get("userId")));

        log.info("Processing challenge check for userId: {}, jobExecutionId: {}", userId, jobExecutionId);

        User user = userRepository.getById(userId);
        challengeEvaluator.evaluate(user);

        log.info("Challenge evaluation completed for userId: {}", userId);
    }

    private void acknowledgeMessage(MapRecord<String, Object, Object> message) {
        redisTemplate.opsForStream().acknowledge(CONSUMER_GROUP, message);
    }

    private void logProcessingError(MapRecord<String, Object, Object> message, Exception e) {
        Map<Object, Object> payload = message.getValue();
        String userId = String.valueOf(payload.get("userId"));
        String jobExecutionId = String.valueOf(payload.get("jobExecutionId"));

        log.error("Failed to process challenge check event - userId: {}, jobExecutionId: {}",
            userId, jobExecutionId, e);
    }
}
