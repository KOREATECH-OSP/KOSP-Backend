package io.swkoreatech.kosp.domain.notification.event;

import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import lombok.Getter;

@Getter
public class NotificationEvent {

    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long referenceId;

    private NotificationEvent(Long userId, NotificationType type, String title, String message, Long referenceId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
    }

    public static NotificationEvent of(Long userId, NotificationType type, String title, String message, Long referenceId) {
        return new NotificationEvent(userId, type, title, message, referenceId);
    }
}
