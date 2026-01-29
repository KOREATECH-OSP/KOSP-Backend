package io.swkoreatech.kosp.domain.notification.util;

import io.swkoreatech.kosp.domain.notification.model.NotificationType;

import java.util.Map;

public class NotificationMessageBuilder {

    public static String buildTitle(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            case ARTICLE_REPORTED -> "게시글 신고 접수";
            case COMMENT_REPORTED -> "댓글 신고 접수";
            case POINT_EARNED -> "포인트 변경";
            case CHALLENGE_ACHIEVED -> "챌린지 달성";
            case SYSTEM -> "시스템 알림";
        };
    }

    public static String buildMessage(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            case ARTICLE_REPORTED -> "회원님의 게시글이 신고되었습니다.";
            case COMMENT_REPORTED -> "회원님의 댓글이 신고되었습니다.";
            case POINT_EARNED -> buildPointMessage(payload);
            case CHALLENGE_ACHIEVED -> buildChallengeMessage(payload);
            case SYSTEM -> buildSystemMessage(payload);
        };
    }

    public static Long extractReferenceId(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            case ARTICLE_REPORTED -> extractLong(payload, "articleId");
            case COMMENT_REPORTED -> extractLong(payload, "commentId");
            case CHALLENGE_ACHIEVED -> extractLong(payload, "challengeId");
            default -> null;
        };
    }

    private static String buildPointMessage(Map<String, Object> payload) {
        Integer amount = extractInteger(payload, "amount");
        String reason = extractString(payload, "reason");

        if (amount == null) {
            return "포인트가 변경되었습니다.";
        }

        if (amount > 0) {
            return formatPointMessage(amount, reason, "획득했습니다");
        }
        return formatPointMessage(Math.abs(amount), reason, "차감되었습니다");
    }

    private static String formatPointMessage(Integer absAmount, String reason, String action) {
        if (reason == null || reason.isBlank()) {
            return String.format("%d포인트를 %s.", absAmount, action);
        }
        return String.format("%d포인트를 %s. (%s)", absAmount, action, reason);
    }

    private static String buildChallengeMessage(Map<String, Object> payload) {
        String challengeName = extractString(payload, "challengeName");
        if (challengeName == null) {
            return "챌린지를 달성했습니다!";
        }
        return String.format("'%s' 챌린지를 달성했습니다!", challengeName);
    }

    private static String buildSystemMessage(Map<String, Object> payload) {
        String message = extractString(payload, "message");
        if (message == null) {
            return "시스템 알림";
        }
        return message;
    }

    private static String extractString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value instanceof String ? (String) value : null;
    }

    private static Integer extractInteger(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }

    private static Long extractLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return null;
    }
}
