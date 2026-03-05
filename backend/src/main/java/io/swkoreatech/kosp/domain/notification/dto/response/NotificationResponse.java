package io.swkoreatech.kosp.domain.notification.dto.response;

import io.swkoreatech.kosp.domain.notification.model.Notification;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO.
 *
 * @param id 알림 ID
 * @param type 알림 유형
 * @param title 제목
 * @param message 메시지
 * @param referenceId 참조 ID
 * @param isRead 읽음 여부
 * @param createdAt 생성일시
 */
public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String message,
    Long referenceId,
    boolean isRead,
    LocalDateTime createdAt
) {
    /**
     * 알림 엔티티로부터 응답을 생성한다.
     *
     * @param notification 알림 엔티티
     * @return 알림 응답
     */
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
