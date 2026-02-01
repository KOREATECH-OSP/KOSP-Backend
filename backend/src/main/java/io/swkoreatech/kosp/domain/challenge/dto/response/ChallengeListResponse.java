package io.swkoreatech.kosp.domain.challenge.dto.response;

import io.swkoreatech.kosp.domain.challenge.model.ImageResourceType;
import java.util.List;

public record ChallengeListResponse(
    List<ChallengeResponse> challenges,
    ChallengeSummary summary
) {
    public record ChallengeResponse(
        Long id,
        String title,
        String description,
        String category,
        Integer progress,
        Boolean isCompleted,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer tier,
        Integer point
    ) {}

    public record ChallengeSummary(
        Long totalChallenges,
        Long completedCount,
        Double overallProgress,
        Integer totalEarnedPoints
    ) {}
}
