package io.swkoreatech.kosp.common.event;

import java.time.LocalDateTime;

public record ChallengeCompletedEvent(
    Long userId,
    Long challengeId,
    String challengeName,
    Integer pointsAwarded,
    LocalDateTime completedAt,
    String messageId
) {}
