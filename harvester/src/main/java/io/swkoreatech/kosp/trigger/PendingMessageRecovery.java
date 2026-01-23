package io.swkoreatech.kosp.trigger;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.launcher.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingMessageRecovery {

    private static final long MAX_PENDING_COUNT = 1000L;

    @Value("${harvester.redis.stream.key}")
    private String streamKey;

    @Value("${harvester.redis.stream.consumer-group}")
    private String consumerGroup;

    @Value("${harvester.redis.stream.consumer-name}")
    private String consumerName;

    private final StringRedisTemplate redisTemplate;
    private final PriorityJobLauncher jobLauncher;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverPendingMessages() {
        PendingMessages pendingMessages = fetchPendingMessages();
        if (pendingMessages.isEmpty()) {
            log.info("No pending messages to recover");
            return;
        }

        log.info("Recovering {} pending messages", pendingMessages.size());
        processPendingMessages(pendingMessages);
    }

    private PendingMessages fetchPendingMessages() {
        Consumer consumer = Consumer.from(consumerGroup, consumerName);
        return redisTemplate.opsForStream().pending(
            streamKey,
            consumer,
            Range.unbounded(),
            MAX_PENDING_COUNT
        );
    }

    private void processPendingMessages(PendingMessages pendingMessages) {
        for (PendingMessage pending : pendingMessages) {
            processAndAcknowledge(pending);
        }
    }

    private void processAndAcknowledge(PendingMessage pending) {
        String messageId = pending.getIdAsString();

        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
            .range(streamKey, Range.closed(messageId, messageId));

        if (records == null || records.isEmpty()) {
            acknowledgeMessage(messageId);
            return;
        }

        MapRecord<String, Object, Object> record = records.get(0);
        processMessage(record);
        acknowledgeMessage(messageId);
    }

    private void processMessage(MapRecord<String, Object, Object> record) {
        Object userIdObj = record.getValue().get("userId");
        if (userIdObj == null) {
            log.warn("Pending message {} has no userId", record.getId());
            return;
        }

        Long userId = Long.parseLong(userIdObj.toString());
        log.info("Recovering pending job for user {}", userId);
        jobLauncher.submit(userId, Priority.HIGH);
    }

    private void acknowledgeMessage(String messageId) {
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, messageId);
        log.debug("Acknowledged pending message {}", messageId);
    }
}
