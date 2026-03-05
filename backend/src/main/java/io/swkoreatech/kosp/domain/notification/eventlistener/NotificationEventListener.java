package io.swkoreatech.kosp.domain.notification.eventlistener;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.common.repository.ProcessedMessageRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.domain.notification.service.NotificationService;
import io.swkoreatech.kosp.domain.point.model.PointSource;
import io.swkoreatech.kosp.domain.point.service.PointService;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 이벤트 리스너.
 * 알림 이벤트, 챌린지 완료 메시지, 포인트 변경 메시지를 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ProcessedMessageRepository processedMessageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PointService pointService;
    private final UserRepository userRepository;

    /**
     * 알림 이벤트를 비동기로 처리하여 알림을 생성하고 전송한다.
     *
     * @param event 알림 이벤트
     */
    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received NotificationEvent for user {}: {}", 
            event.getUserId(), event.getTitle());
        notificationService.createAndSend(event);
    }

    /**
     * 챌린지 완료 메시지를 수신하여 알림을 발행한다.
     *
     * @param event 챌린지 완료 이벤트
     * @param channel RabbitMQ 채널
     * @param deliveryTag 메시지 전달 태그
     * @throws IOException 메시지 ACK/NACK 처리 중 예외
     */
    @RabbitListener(queues = QueueNames.CHALLENGE_COMPLETED)
    @Transactional
    public void handleChallengeCompleted(
            ChallengeCompletedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        
        if (processedMessageRepository.existsByMessageId(event.messageId())) {
            log.info("Duplicate message: {}", event.messageId());
            channel.basicAck(deliveryTag, false);
            return;
        }
        
        try {
            NotificationEvent notificationEvent = NotificationEvent.of(
                event.userId(),
                NotificationType.CHALLENGE_ACHIEVED,
                "챌린지 완료",
                String.format("%s 챌린지를 완료했습니다.", event.challengeName()),
                event.challengeId()
            );
            eventPublisher.publishEvent(notificationEvent);
            
            processedMessageRepository.save(
                new ProcessedMessage(event.messageId(), "ChallengeCompletedEvent")
            );
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process challenge completed notification", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 포인트 변경 메시지를 수신하여 포인트를 처리하고 알림을 발행한다.
     *
     * @param event 포인트 변경 이벤트
     * @param channel RabbitMQ 채널
     * @param deliveryTag 메시지 전달 태그
     * @throws IOException 메시지 ACK/NACK 처리 중 예외
     */
    @RabbitListener(queues = QueueNames.POINT_CHANGED)
    @Transactional
    public void handlePointChanged(
            PointChangedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        
        if (processedMessageRepository.existsByMessageId(event.messageId())) {
            log.info("Duplicate message: {}", event.messageId());
            channel.basicAck(deliveryTag, false);
            return;
        }
        
        try {
            User user = userRepository.getById(event.userId());
            pointService.changePoint(user, event.amount(), event.reason(), PointSource.valueOf(event.source()));
            
            NotificationEvent notificationEvent = NotificationEvent.of(
                event.userId(),
                NotificationType.POINT_EARNED,
                "포인트 획득",
                String.format("%d포인트를 획득했습니다. (%s)", event.amount(), event.reason()),
                null
            );
            eventPublisher.publishEvent(notificationEvent);
            
            processedMessageRepository.save(
                new ProcessedMessage(event.messageId(), "PointChangedEvent")
            );
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process point changed notification", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
