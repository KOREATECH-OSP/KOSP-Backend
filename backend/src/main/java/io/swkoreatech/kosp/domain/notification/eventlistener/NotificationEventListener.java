package io.swkoreatech.kosp.domain.notification.eventlistener;

import com.rabbitmq.client.Channel;
import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.common.repository.ProcessedMessageRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.connection.ConnectionFactory")
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ProcessedMessageRepository processedMessageRepository;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received NotificationEvent for user {}: {}", 
            event.getUserId(), event.getTitle());
        notificationService.createAndSend(event);
    }

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
                String.format("%s 챌린지를 완료했습니다. %d포인트를 획득했습니다.", 
                    event.challengeName(), event.pointsAwarded()),
                event.challengeId()
            );
            notificationService.createAndSend(notificationEvent);
            
            processedMessageRepository.save(
                new ProcessedMessage(event.messageId(), "ChallengeCompletedEvent")
            );
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process challenge completed notification", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

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
            NotificationEvent notificationEvent = NotificationEvent.of(
                event.userId(),
                NotificationType.POINT_EARNED,
                "포인트 획득",
                String.format("%d포인트를 획득했습니다. (%s)", event.amount(), event.reason()),
                null
            );
            notificationService.createAndSend(notificationEvent);
            
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
