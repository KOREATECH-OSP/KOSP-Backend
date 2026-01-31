package io.swkoreatech.kosp.notification.listener;

import com.rabbitmq.client.Channel;
import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import io.swkoreatech.kosp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationService notificationService;
    
    @RabbitListener(queues = QueueNames.CHALLENGE_COMPLETED)
    public void handleChallengeCompleted(
            ChallengeCompletedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        
        try {
            notificationService.sendChallengeNotification(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
    
    @RabbitListener(queues = QueueNames.POINT_CHANGED)
    public void handlePointChanged(
            PointChangedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        
        try {
            notificationService.sendPointNotification(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
