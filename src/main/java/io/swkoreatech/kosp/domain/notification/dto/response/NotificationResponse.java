package io.swkoreatech.kosp.domain.notification.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.notification.model.Notification;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String message,
    Long referenceId,
    boolean isRead,
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceId(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
