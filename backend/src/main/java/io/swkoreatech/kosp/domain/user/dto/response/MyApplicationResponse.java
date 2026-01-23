package io.swkoreatech.kosp.domain.user.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import lombok.Builder;

@Builder
public record MyApplicationResponse(
    Long applicationId,
    String status,
    String reason,
    String portfolioUrl,
    LocalDateTime appliedAt,
    RecruitSummary recruit
) {
    public static MyApplicationResponse from(RecruitApply apply) {
        return MyApplicationResponse.builder()
            .applicationId(apply.getId())
            .status(apply.getStatus().name())
            .reason(apply.getReason())
            .portfolioUrl(apply.getPortfolioUrl())
            .appliedAt(apply.getCreatedAt())
            .recruit(RecruitSummary.from(apply.getRecruit()))
            .build();
    }

    @Builder
    public record RecruitSummary(
        Long id,
        String title,
        String teamName,
        String status,
        LocalDateTime endDate
    ) {
        public static RecruitSummary from(Recruit recruit) {
            return RecruitSummary.builder()
                .id(recruit.getId())
                .title(recruit.getTitle())
                .teamName(recruit.getTeam().getName())
                .status(recruit.getStatus().name())
                .endDate(recruit.getEndDate())
                .build();
        }
    }
}
