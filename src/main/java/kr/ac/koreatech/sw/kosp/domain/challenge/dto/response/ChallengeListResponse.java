package kr.ac.koreatech.sw.kosp.domain.challenge.dto.response;

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
        Long current,
        Long total,
        Boolean isCompleted,
        String imageUrl,
        Integer tier
    ) {}

    public record ChallengeSummary(
        Long totalChallenges,
        Long completedCount,
        Double overallProgress
    ) {}
}
