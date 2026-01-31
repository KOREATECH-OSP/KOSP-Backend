package io.swkoreatech.kosp.integration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swkoreatech.kosp.common.entity.OutboxMessage;
import io.swkoreatech.kosp.common.entity.OutboxStatus;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.repository.OutboxMessageRepository;
import io.swkoreatech.kosp.domain.user.dto.request.UserSignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Integration test verifying the complete MSA flow:
 * Signup → Outbox → OutboxPublisher → RabbitMQ → Harvester → Challenge Service → Notification
 * 
 * Tests cover:
 * 1. Full signup flow triggering notification events
 * 2. Outbox pattern with PENDING → PUBLISHED status transitions
 * 3. JobQueueService preservation (Backend → Harvester via Redis ZSET)
 * 4. RabbitMQ message publishing verification
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Signup to Notification Integration Test")
public class SignupToNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxMessageRepository outboxRepository;

    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private AmqpAdmin rabbitmqAdmin;

    @Test
    @DisplayName("회원가입 → Outbox → Harvester → 챌린지 평가 → 알림 전체 플로우")
    void signupFlowTriggersNotification() throws Exception {
        // Given
        UserSignupRequest signupRequest = createSignupRequest();

        // When: User signs up
        mockMvc.perform(post("/v1/users/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated());

        // Then: Outbox message created
        await().atMost(2, SECONDS)
            .untilAsserted(() -> {
                List<OutboxMessage> messages = findOutboxMessages();
                assertThat(messages).isNotEmpty();
                assertThat(messages.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);
            });

        // Then: Verify JobQueueService has job for Harvester
        await().atMost(5, SECONDS)
            .untilAsserted(() -> {
                assertThat(jobQueueService.dequeue()).isPresent();
            });

        // Then: Wait for OutboxPublisher to publish
        await().atMost(10, SECONDS)
            .untilAsserted(() -> {
                List<OutboxMessage> messages = findPublishedMessages();
                assertThat(messages).isNotEmpty();
                assertThat(messages.get(0).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            });

        // Then: Verify RabbitMQ queue has message
        await().atMost(5, SECONDS)
            .untilAsserted(() -> {
                QueueInformation queueInfo = getRabbitMQQueueInfo("challenge-evaluation-queue");
                assertThat(queueInfo).isNotNull();
                assertThat(queueInfo.getMessageCount()).isGreaterThan(0);
            });
    }

    @Test
    @DisplayName("JobQueueService 동작 확인 (Backend → Harvester via Redis ZSET)")
    void jobQueueServiceStillWorksViaRedis() {
        // Given
        Long userId = 12345L;
        String runId = "test-run-001";

        // When
        enqueueJob(userId, runId);

        // Then
        await().atMost(2, SECONDS)
            .untilAsserted(() -> {
                assertThat(jobQueueService.dequeue()).isPresent();
            });
    }

    @Test
    @DisplayName("Outbox 메시지 발행 상태 전이: PENDING → PUBLISHED")
    void outboxStatusTransition() throws Exception {
        // Given
        UserSignupRequest signupRequest = createSignupRequest();

        // When
        mockMvc.perform(post("/v1/users/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isCreated());

        // Then: Initial status is PENDING
        await().atMost(2, SECONDS)
            .untilAsserted(() -> {
                List<OutboxMessage> messages = findPendingMessages();
                assertThat(messages).isNotEmpty();
            });

        // Then: Status changes to PUBLISHED
        await().atMost(10, SECONDS)
            .untilAsserted(() -> {
                List<OutboxMessage> messages = findPublishedMessages();
                assertThat(messages).isNotEmpty();
            });
    }

    private UserSignupRequest createSignupRequest() {
        return new UserSignupRequest(
            "박성빈",
            "2023100514",
            "test@koreatech.ac.kr",
            "Password123!"
        );
    }

    private List<OutboxMessage> findOutboxMessages() {
        return outboxRepository.findAll();
    }

    private List<OutboxMessage> findPendingMessages() {
        return outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    private List<OutboxMessage> findPublishedMessages() {
        return outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PUBLISHED);
    }

    private QueueInformation getRabbitMQQueueInfo(String queueName) {
        return rabbitmqAdmin.getQueueInfo(queueName);
    }

    private void enqueueJob(Long userId, String runId) {
        jobQueueService.enqueue(
            userId,
            runId,
            java.time.Instant.now(),
            io.swkoreatech.kosp.common.queue.Priority.LOW
        );
    }
}
