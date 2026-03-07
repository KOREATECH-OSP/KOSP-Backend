package io.swkoreatech.kosp.common.event;

import java.time.LocalDateTime;

/**
 * 챌린지 완료 이벤트.
 *
 * <p>사용자가 챌린지를 달성했을 때 발행되며,
 * 포인트 지급 및 알림 처리 등 후속 작업을 트리거한다.</p>
 *
 * @param userId         챌린지를 완료한 사용자 ID
 * @param challengeId    완료된 챌린지 ID
 * @param challengeName  완료된 챌린지 이름
 * @param pointsAwarded  지급된 포인트
 * @param completedAt    챌린지 완료 시각
 * @param messageId      멱등성 보장을 위한 메시지 ID
 */
public record ChallengeCompletedEvent(
    Long userId,
    Long challengeId,
    String challengeName,
    Integer pointsAwarded,
    LocalDateTime completedAt,
    String messageId
) {
}
