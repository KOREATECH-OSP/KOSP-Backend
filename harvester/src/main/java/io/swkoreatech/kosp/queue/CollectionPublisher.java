package io.swkoreatech.kosp.queue;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GitHub 수집 요청을 RabbitMQ에 발행하는 컴포넌트.
 *
 * <p>다음 고정 슬롯(하루 3회)으로의 예약 발행과 특정 시각으로의 재시도 발행을 담당한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionPublisher {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int[] SCHEDULED_HOURS = {1, 12, 18};

    private final RabbitTemplate rabbitTemplate;

    /**
     * 다음 고정 슬롯(06:00 / 14:00 / 22:00 KST)으로 수집을 예약한다.
     */
    public void scheduleNextSlot(Long userId) {
        Instant nextSlot = getNextScheduledSlot();
        int delayMs = (int) Math.max(0, Duration.between(Instant.now(), nextSlot).toMillis());
        publishWithRetry(userId, delayMs);
        log.info("Scheduled next collection for user {} at {} (KST)", userId,
            nextSlot.atZone(KST));
    }

    /**
     * 지정한 시각으로 수집 재시도를 예약한다.
     */
    public void scheduleRetry(Long userId, Instant scheduledAt) {
        int delayMs = (int) Math.max(0, Duration.between(Instant.now(), scheduledAt).toMillis());
        publishWithRetry(userId, delayMs);
        log.info("Scheduled retry for user {} at {}", userId, scheduledAt);
    }

    private Instant getNextScheduledSlot() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        for (int hour : SCHEDULED_HOURS) {
            ZonedDateTime slot = now.toLocalDate().atStartOfDay(KST).plusHours(hour);
            if (slot.isAfter(now)) {
                return slot.toInstant();
            }
        }
        // 오늘 슬롯이 모두 지난 경우 다음날 첫 슬롯
        return now.toLocalDate().plusDays(1).atStartOfDay(KST)
            .plusHours(SCHEDULED_HOURS[0]).toInstant();
    }

    private void publishWithRetry(Long userId, int delayMs) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                publish(userId, delayMs);
                return;
            } catch (Exception e) {
                if (attempt == 3) {
                    log.error("Failed to publish after 3 attempts for user {}", userId, e);
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void publish(Long userId, int delayMs) {
        GithubCollectionRequest dto = new GithubCollectionRequest(userId);
        rabbitTemplate.convertAndSend(
            QueueNames.GITHUB_COLLECTION_EXCHANGE,
            QueueNames.GITHUB_COLLECTION,
            dto,
            message -> {
                message.getMessageProperties().setHeader("x-delay", delayMs);
                return message;
            }
        );
    }
}
