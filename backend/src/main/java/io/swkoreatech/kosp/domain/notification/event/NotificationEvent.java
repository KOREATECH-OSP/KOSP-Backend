package io.swkoreatech.kosp.domain.notification.event;

import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import lombok.Getter;

import java.util.Map;

@Getter
public class NotificationEvent {

    private final Long userId;
    private final NotificationType type;
    private final Map<String, Object> payload;

    private NotificationEvent(Long userId, NotificationType type, Map<String, Object> payload) {
        this.userId = userId;
        this.type = type;
        this.payload = payload;
    }

    public static NotificationEvent of(Long userId, NotificationType type, Map<String, Object> payload) {
        return new NotificationEvent(userId, type, payload);
    }
}
