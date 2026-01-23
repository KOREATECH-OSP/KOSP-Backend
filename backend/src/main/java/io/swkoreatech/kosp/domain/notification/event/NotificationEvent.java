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

    public static NotificationEvent articleReported(Long userId, Long articleId) {
        return new NotificationEvent(
            userId,
            NotificationType.ARTICLE_REPORTED,
            "게시글 신고 접수",
            "회원님의 게시글이 신고되었습니다.",
            articleId
        );
    }

    public static NotificationEvent commentReported(Long userId, Long commentId) {
        return new NotificationEvent(
            userId,
            NotificationType.COMMENT_REPORTED,
            "댓글 신고 접수",
            "회원님의 댓글이 신고되었습니다.",
            commentId
        );
    }
}
