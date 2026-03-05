package io.swkoreatech.kosp.common.event;

import java.time.LocalDateTime;

/**
 * 챌린지 평가 요청 이벤트.
 *
 * <p>특정 사용자의 챌린지 달성 여부를 평가하도록 요청할 때 사용된다.</p>
 *
 * @param userId    평가 대상 사용자 ID
 * @param messageId 멱등성 보장을 위한 메시지 ID
 * @param timestamp 요청 시각
 */
public record ChallengeEvaluationRequest(
    Long userId,
    String messageId,
    LocalDateTime timestamp
) {}
