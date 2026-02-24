package io.swkoreatech.kosp.domain.challenge.dto.response;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;
import java.util.List;

public record ChallengeListResponse(
    List<ChallengeResponse> challenges,
    ChallengeSummary summary
) {
    public static ChallengeListResponse from(List<ChallengeResponse> challenges, ChallengeSummary summary) {
        return new ChallengeListResponse(challenges, summary);
    }

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
    ) {
        public static ChallengeResponse from(Challenge challenge, int progress, boolean isCompleted) {
            return new ChallengeResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                "general",
                progress,
                isCompleted,
                challenge.getImageResource(),
                challenge.getImageResourceType(),
                challenge.getTier(),
                challenge.getPoint()
            );
        }
    }

    public record ChallengeSummary(
        Long totalChallenges,
        Long completedCount,
        Double overallProgress,
        Integer totalEarnedPoints
    ) {
        public static ChallengeSummary from(
            long totalChallenges,
            long completedCount,
            double overallProgress,
            int totalEarnedPoints
        ) {
            return new ChallengeSummary(totalChallenges, completedCount, overallProgress, totalEarnedPoints);
        }
    }
}
