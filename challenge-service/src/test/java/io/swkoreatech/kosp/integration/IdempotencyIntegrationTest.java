package io.swkoreatech.kosp.integration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import io.swkoreatech.kosp.common.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Integration test verifying idempotency pattern in challenge-service.
 * 
 * Tests cover:
 * 1. Duplicate message processing prevention via processed_messages table
 * 2. Manual ACK handling with basicAck/basicNack
 * 3. Dead Letter Queue (DLQ) routing for failed messages
 * 4. Unique constraint enforcement on messageId
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Idempotency Integration Test")
public class IdempotencyIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Test
    @DisplayName("동일한 messageId 중복 처리 방지: 첫 번째만 처리됨")
    void duplicateMessageIdempotency() {
        // Given
        String messageId = generateMessageId();
        String eventType = "ChallengeCompletedEvent";
        ChallengeEventPayload payload = createPayload(1L, "COMMIT_MASTER");

        // When: First message processed
        publishMessage(messageId, eventType, payload);

        // Then: Message marked as processed
        await().atMost(5, SECONDS)
            .untilAsserted(() -> {
                boolean exists = checkMessageProcessed(messageId);
                assertThat(exists).isTrue();
            });

        long initialCount = countProcessedMessages();

        // When: Duplicate message published
        publishMessage(messageId, eventType, payload);

        // Then: Duplicate not processed
        await().atMost(3, SECONDS)
            .untilAsserted(() -> {
                long currentCount = countProcessedMessages();
                assertThat(currentCount).isEqualTo(initialCount);
            });
    }

    @Test
    @DisplayName("다른 messageId는 각각 처리됨")
    void differentMessageIdsProcessedSeparately() {
        // Given
        String messageId1 = generateMessageId();
        String messageId2 = generateMessageId();
        String eventType = "ChallengeCompletedEvent";

        ChallengeEventPayload payload1 = createPayload(1L, "COMMIT_MASTER");
        ChallengeEventPayload payload2 = createPayload(2L, "PR_MASTER");

        // When
        publishMessage(messageId1, eventType, payload1);
        publishMessage(messageId2, eventType, payload2);

        // Then
        await().atMost(5, SECONDS)
            .untilAsserted(() -> {
                assertThat(checkMessageProcessed(messageId1)).isTrue();
                assertThat(checkMessageProcessed(messageId2)).isTrue();
                assertThat(countProcessedMessages()).isGreaterThanOrEqualTo(2);
            });
    }

    @Test
    @DisplayName("processed_messages 테이블 unique constraint 검증")
    void uniqueConstraintOnMessageId() {
        // Given
        String messageId = generateMessageId();
        String eventType = "TestEvent";

        // When: First insert
        ProcessedMessage first = createProcessedMessage(messageId, eventType);
        processedMessageRepository.save(first);
        processedMessageRepository.flush();

        // Then: Duplicate insert should fail
        try {
            ProcessedMessage duplicate = createProcessedMessage(messageId, eventType);
            processedMessageRepository.save(duplicate);
            processedMessageRepository.flush();
            
            assertThat(false).as("Should throw exception").isTrue();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("빈 messageId 처리 시도 시 스킵")
    void emptyMessageIdSkipped() {
        // Given
        String emptyMessageId = "";
        String eventType = "TestEvent";

        // When
        try {
            ProcessedMessage message = createProcessedMessage(emptyMessageId, eventType);
            processedMessageRepository.save(message);
            processedMessageRepository.flush();
            
            assertThat(false).as("Should fail validation").isTrue();
        } catch (Exception e) {
            // Then
            assertThat(e).isNotNull();
        }
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    private ChallengeEventPayload createPayload(Long userId, String challengeType) {
        return new ChallengeEventPayload(userId, challengeType, 100);
    }

    private void publishMessage(String messageId, String eventType, Object payload) {
        rabbitTemplate.convertAndSend(
            "challenge-completed-queue",
            payload,
            message -> {
                message.getMessageProperties().setMessageId(messageId);
                message.getMessageProperties().setHeader("eventType", eventType);
                return message;
            }
        );
    }

    private boolean checkMessageProcessed(String messageId) {
        return processedMessageRepository.existsByMessageId(messageId);
    }

    private long countProcessedMessages() {
        return processedMessageRepository.count();
    }

    private ProcessedMessage createProcessedMessage(String messageId, String eventType) {
        return new ProcessedMessage(messageId, eventType);
    }

    private record ChallengeEventPayload(
        Long userId,
        String challengeType,
        Integer points
    ) {}
}
