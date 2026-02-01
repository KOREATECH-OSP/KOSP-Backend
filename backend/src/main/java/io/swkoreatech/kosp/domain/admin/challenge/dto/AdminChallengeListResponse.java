package io.swkoreatech.kosp.domain.admin.challenge.dto;

import io.swkoreatech.kosp.domain.challenge.model.ImageResourceType;
import java.util.List;

public record AdminChallengeListResponse(
    List<ChallengeInfo> challenges
) {
    public record ChallengeInfo(
        Long id,
        String name,
        String description,
        String condition,
        Integer tier,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer point
    ) {}
}
