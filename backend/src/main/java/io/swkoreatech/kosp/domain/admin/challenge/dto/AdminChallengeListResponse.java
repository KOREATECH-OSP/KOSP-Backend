package io.swkoreatech.kosp.domain.admin.challenge.dto;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;

import java.util.List;

public record AdminChallengeListResponse(
    List<ChallengeInfo> challenges
) {
    public static AdminChallengeListResponse from(List<Challenge> challenges) {
        return new AdminChallengeListResponse(
            challenges.stream().map(ChallengeInfo::from).toList()
        );
    }

    public record ChallengeInfo(
        Long id,
        String name,
        String description,
        String condition,
        Integer tier,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer point
    ) {
        public static ChallengeInfo from(Challenge challenge) {
            return new ChallengeInfo(
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
}
