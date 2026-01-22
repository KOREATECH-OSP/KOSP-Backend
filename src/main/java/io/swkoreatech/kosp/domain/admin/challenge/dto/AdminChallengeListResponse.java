package io.swkoreatech.kosp.domain.admin.challenge.dto;

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
        String imageUrl,
        Integer point
    ) {}
}
