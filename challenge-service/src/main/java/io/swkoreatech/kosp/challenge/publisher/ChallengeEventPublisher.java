package io.swkoreatech.kosp.challenge.publisher;

import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 챌린지 관련 이벤트를 RabbitMQ로 발행하는 퍼블리셔.
 *
 * <p>포인트 변경 이벤트 및 챌린지 완료 이벤트를 메시지 큐에 전송하여
 * 다른 서비스에서 비동기적으로 처리할 수 있도록 한다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 포인트 변경 이벤트를 RabbitMQ에 발행한다.
     *
     * @param userId 포인트가 변경될 사용자 ID
     * @param amount 변경할 포인트 양 (양수: 지급, 음수: 차감)
     * @param reason 포인트 변경 사유
     * @param source 포인트 변경 출처
     */
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

            log.info("Published PointChangedEvent to RabbitMQ: userId={}, points={}", userId, amount);
        } catch (Exception e) {
            log.error("Failed to publish PointChangedEvent", e);
        }
    }

    /**
     * 챌린지 완료 이벤트를 RabbitMQ에 발행한다.
     *
     * @param userId 챌린지를 달성한 사용자 ID
     * @param challengeId 달성된 챌린지 ID
     * @param challengeName 달성된 챌린지 이름
     * @param pointsAwarded 지급된 포인트
     */
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

            log.info("Published ChallengeCompletedEvent to RabbitMQ: userId={}, challengeId={}", userId, challengeId);
        } catch (Exception e) {
            log.error("Failed to publish ChallengeCompletedEvent", e);
        }
    }
}
