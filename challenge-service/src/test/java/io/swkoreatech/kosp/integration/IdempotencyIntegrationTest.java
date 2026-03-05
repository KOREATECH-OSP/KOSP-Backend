package io.swkoreatech.kosp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import io.swkoreatech.kosp.common.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * 2. Unique constraint enforcement on messageId
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Idempotency Integration Test")
public class IdempotencyIntegrationTest {

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Test
    @DisplayName("동일한 messageId 중복 처리 방지: 첫 번째만 저장됨")
    void duplicateMessageIdempotency() {
        // Given
        String messageId = generateMessageId();
        String eventType = "ChallengeCompletedEvent";

        // When: First message processed
        ProcessedMessage first = createProcessedMessage(messageId, eventType);
        processedMessageRepository.save(first);
        processedMessageRepository.flush();

        // Then: Message marked as processed
        assertThat(processedMessageRepository.existsByMessageId(messageId)).isTrue();

        long initialCount = processedMessageRepository.count();

        // When: Duplicate message attempted
        assertThatThrownBy(() -> {
            ProcessedMessage duplicate = createProcessedMessage(messageId, eventType);
            processedMessageRepository.save(duplicate);
            processedMessageRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("다른 messageId는 각각 처리됨")
    void differentMessageIdsProcessedSeparately() {
        // Given
        String messageId1 = generateMessageId();
        String messageId2 = generateMessageId();
        String eventType = "ChallengeCompletedEvent";

        // When
        processedMessageRepository.save(createProcessedMessage(messageId1, eventType));
        processedMessageRepository.save(createProcessedMessage(messageId2, eventType));
        processedMessageRepository.flush();

        // Then
        assertThat(processedMessageRepository.existsByMessageId(messageId1)).isTrue();
        assertThat(processedMessageRepository.existsByMessageId(messageId2)).isTrue();
        assertThat(processedMessageRepository.count()).isGreaterThanOrEqualTo(2);
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
        assertThatThrownBy(() -> {
            ProcessedMessage duplicate = createProcessedMessage(messageId, eventType);
            processedMessageRepository.save(duplicate);
            processedMessageRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    private ProcessedMessage createProcessedMessage(String messageId, String eventType) {
        return new ProcessedMessage(messageId, eventType);
    }
}
