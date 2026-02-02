package io.swkoreatech.kosp.challenge.publisher;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPointChange(Long userId, Integer amount, String reason, String source) {
        try {
            PointChangedEvent rabbitEvent = new PointChangedEvent(
                userId,
                amount,
                reason,
                source,
                UUID.randomUUID().toString()
            );

            rabbitTemplate.convertAndSend(
                QueueNames.POINT_CHANGED,
                rabbitEvent
            );

            log.info("Published PointChangedEvent to RabbitMQ: userId={}, points={}", 
                userId, amount);
        } catch (Exception e) {
            log.error("Failed to publish PointChangedEvent", e);
        }
    }

}
