package io.swkoreatech.kosp.domain.notification.event;

import io.swkoreatech.kosp.domain.notification.model.NotificationType;

import lombok.Getter;

/**
 * 알림 이벤트.
 * 알림 생성 시 발행되는 애플리케이션 이벤트이다.
 */
@Getter
public class NotificationEvent {

    private final Long userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Long referenceId;

    private NotificationEvent(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long referenceId
    ) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
    }

    /**
     * 알림 이벤트를 생성한다.
     *
     * @param userId 대상 사용자 ID
     * @param type 알림 유형
     * @param title 제목
     * @param message 메시지
     * @param referenceId 참조 ID
     * @return 알림 이벤트
     */
    public static NotificationEvent of(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Long referenceId
    ) {
        return new NotificationEvent(userId, type, title, message, referenceId);
    }
}
