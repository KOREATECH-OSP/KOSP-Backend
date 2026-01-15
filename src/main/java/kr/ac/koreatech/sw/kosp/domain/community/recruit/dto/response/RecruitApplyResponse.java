package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;

public record RecruitApplyResponse(
    Long id,
    Long userId,
    String userName,
    String userEmail,
    String reason,
    String portfolioUrl,
    String status,
    LocalDateTime appliedAt
) {
    public static RecruitApplyResponse from(RecruitApply apply) {
        return new RecruitApplyResponse(
            apply.getId(),
            apply.getUser().getId(),
            apply.getUser().getName(),
            apply.getUser().getKutEmail(),
            apply.getReason(),
            apply.getPortfolioUrl(),
            apply.getStatus().name(),
            apply.getCreatedAt()
        );
    }
}
