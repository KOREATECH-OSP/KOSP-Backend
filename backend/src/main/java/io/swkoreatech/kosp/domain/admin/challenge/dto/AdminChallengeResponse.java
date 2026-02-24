package io.swkoreatech.kosp.domain.admin.challenge.dto;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;

public record AdminChallengeResponse(
    Long id,
    String name,
    String description,
    String condition,
    Integer tier,
    String imageResource,
    ImageResourceType imageResourceType,
    Integer point
) {
    public static AdminChallengeResponse from(Challenge challenge) {
        return new AdminChallengeResponse(
            challenge.getId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getCondition(),
            challenge.getTier(),
            challenge.getImageResource(),
            challenge.getImageResourceType(),
            challenge.getPoint()
        );
    }
}
