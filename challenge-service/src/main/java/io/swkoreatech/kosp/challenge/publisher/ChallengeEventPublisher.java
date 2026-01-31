package io.swkoreatech.kosp.challenge.publisher;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointChange(PointChangeEvent event) {
        try {
            PointChangedEvent rabbitEvent = new PointChangedEvent(
                event.user().getId(),
                event.amount(),
                event.reason(),
                event.source().name(),
                UUID.randomUUID().toString()
            );

            rabbitTemplate.convertAndSend(
                QueueNames.POINT_CHANGED,
                rabbitEvent
            );

            log.info("Published PointChangedEvent to RabbitMQ: userId={}, points={}", 
                event.user().getId(), event.amount());
        } catch (Exception e) {
            log.error("Failed to publish PointChangedEvent", e);
        }
    }

    public void publishChallengeCompleted(Long userId, Long challengeId, String challengeName, Integer pointsAwarded) {
        try {
            ChallengeCompletedEvent event = new ChallengeCompletedEvent(
                userId,
                challengeId,
                challengeName,
                pointsAwarded,
                LocalDateTime.now(),
                UUID.randomUUID().toString()
            );

            rabbitTemplate.convertAndSend(
                QueueNames.CHALLENGE_COMPLETED,
                event
            );

            log.info("Published ChallengeCompletedEvent to RabbitMQ: userId={}, challengeId={}", 
                userId, challengeId);
        } catch (Exception e) {
            log.error("Failed to publish ChallengeCompletedEvent", e);
        }
    }
}
