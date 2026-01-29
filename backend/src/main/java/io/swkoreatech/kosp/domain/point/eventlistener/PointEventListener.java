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
        String title = "포인트 변경";
        String message = buildPointMessage(event.amount(), event.reason());
        
        eventPublisher.publishEvent(
            NotificationEvent.of(event.user().getId(), NotificationType.POINT_EARNED, title, message, null)
        );
    }

    private String buildPointMessage(Integer amount, String reason) {
        if (amount > 0) {
            return formatPointMessage(amount, reason, "획득했습니다");
        }
        return formatPointMessage(Math.abs(amount), reason, "차감되었습니다");
    }

    private String formatPointMessage(Integer absAmount, String reason, String action) {
        if (reason == null || reason.isBlank()) {
            return String.format("%d포인트를 %s.", absAmount, action);
        }
        return String.format("%d포인트를 %s. (%s)", absAmount, action, reason);
    }
}
