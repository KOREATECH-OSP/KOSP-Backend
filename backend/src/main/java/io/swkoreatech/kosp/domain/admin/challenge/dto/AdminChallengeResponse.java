package io.swkoreatech.kosp.domain.admin.challenge.dto;

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
}
