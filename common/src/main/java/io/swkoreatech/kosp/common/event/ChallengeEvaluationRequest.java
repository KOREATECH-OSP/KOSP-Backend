package io.swkoreatech.kosp.common.event;

import java.time.LocalDateTime;

public record ChallengeEvaluationRequest(
    Long userId,
    String messageId,
    LocalDateTime timestamp
) {}
