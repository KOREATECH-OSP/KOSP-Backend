package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;

public record RecruitApplyResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    String userProfileImage,
    String reason,
    String portfolioUrl,
    String status,
    LocalDateTime appliedAt
) {
    public static RecruitApplyResponse from(RecruitApply apply) {
        String profileImage = null;
        if (apply.getUser().getGithubUser() != null) {
            profileImage = apply.getUser().getGithubUser().getGithubAvatarUrl();
        }
        return new RecruitApplyResponse(
            apply.getId(),
            apply.getUser().getId(),
            apply.getUser().getName(),
            apply.getUser().getKutEmail(),
            profileImage,
            apply.getReason(),
            apply.getPortfolioUrl(),
            apply.getStatus().name(),
            apply.getCreatedAt()
        );
    }
}
