package io.swkoreatech.kosp.domain.notification.model;

/**
 * 알림 유형.
 * ARTICLE_REPORTED: 게시글 신고, COMMENT_REPORTED: 댓글 신고,
 * CHALLENGE_ACHIEVED: 챌린지 달성, POINT_EARNED: 포인트 획득,
 * TEAM_INVITED: 팀 초대, SYSTEM: 시스템 알림.
 */
public enum NotificationType {

    ARTICLE_REPORTED,
    COMMENT_REPORTED,
    CHALLENGE_ACHIEVED,
    POINT_EARNED,
    TEAM_INVITED,
    COFFEE_CHAT_RECEIVED,
    SYSTEM
}
