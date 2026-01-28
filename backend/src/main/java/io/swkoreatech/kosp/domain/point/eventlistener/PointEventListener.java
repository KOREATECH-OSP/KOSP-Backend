package io.swkoreatech.kosp.domain.point.eventlistener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.point.model.PointSource;
import io.swkoreatech.kosp.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void handlePointChange(PointChangeEvent event) {
        log.info("Processing point change: user={}, amount={}, source={}",
            event.user().getId(), event.amount(), event.source());

        pointService.changePoint(event.user(), event.amount(), event.reason(), event.source());
        
        publishNotificationIfAdmin(event);
    }
    
    private void publishNotificationIfAdmin(PointChangeEvent event) {
        if (event.source() != PointSource.ADMIN) {
            return;
        }
        
        eventPublisher.publishEvent(
            NotificationEvent.pointEarned(event.user().getId(), event.amount(), event.reason())
        );
    }
}
