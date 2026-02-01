package io.swkoreatech.kosp.domain.admin.challenge.dto;

import io.swkoreatech.kosp.domain.challenge.model.ImageResourceType;

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
}
