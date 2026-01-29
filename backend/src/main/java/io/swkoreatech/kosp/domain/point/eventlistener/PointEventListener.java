package io.swkoreatech.kosp.domain.point.eventlistener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handlePointChange(PointChangeEvent event) {
        logPointChange(event);
        pointService.changePoint(event.user(), event.amount(), event.reason(), event.source());
        publishNotification(event);
    }

    private void logPointChange(PointChangeEvent event) {
        log.info("Processing point change: user={}, amount={}, source={}",
            event.user().getId(), event.amount(), event.source());
    }

    private void publishNotification(PointChangeEvent event) {
        Map<String, Object> payload = createPayload(event);
        
        eventPublisher.publishEvent(
            NotificationEvent.of(event.user().getId(), NotificationType.POINT_EARNED, payload)
        );
    }

    private Map<String, Object> createPayload(PointChangeEvent event) {
        return Map.of(
            "amount", event.amount(),
            "reason", event.reason()
        );
    }
}

