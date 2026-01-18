package kr.ac.koreatech.sw.kosp.domain.user.dto.response;

import java.time.LocalDateTime;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;
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
        public static RecruitSummary from(kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit recruit) {
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
