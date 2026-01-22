package io.swkoreatech.kosp.domain.challenge.dto.response;

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
        Integer current,
        Integer total,
        Boolean isCompleted,
        String imageUrl,
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
