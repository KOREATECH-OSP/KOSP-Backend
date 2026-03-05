package io.swkoreatech.kosp.domain.user.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import lombok.Builder;

/**
 * 본인 지원 내역 응답 DTO.
 *
 * @param applicationId 지원 ID
 * @param status 지원 상태
 * @param reason 지원 사유
 * @param portfolioUrl 포트폴리오 URL
 * @param appliedAt 지원 일시
 * @param decisionReason 심사 사유
 * @param recruit 모집 공고 요약 정보
 */
@Builder
public record MyApplicationResponse(
    Long applicationId,
    String status,
    String reason,
    String portfolioUrl,
    LocalDateTime appliedAt,
    String decisionReason,
    RecruitSummary recruit
) {
    /**
     * RecruitApply 엔티티로부터 지원 내역 응답을 생성한다.
     *
     * @param apply 지원 엔티티
     * @return 지원 내역 응답
     */
    public static MyApplicationResponse from(RecruitApply apply) {
        return MyApplicationResponse.builder()
            .applicationId(apply.getId())
            .status(apply.getStatus().name())
            .reason(apply.getReason())
            .portfolioUrl(apply.getPortfolioUrl())
            .appliedAt(apply.getCreatedAt())
            .decisionReason(apply.getDecisionReason())
            .recruit(RecruitSummary.from(apply.getRecruit()))
            .build();
    }

    /**
     * 모집 공고 요약 정보.
     *
     * @param id 모집 공고 ID
     * @param title 모집 공고 제목
     * @param teamName 팀 이름
     * @param status 모집 상태
     * @param endDate 모집 마감일
     */
    @Builder
    public record RecruitSummary(
        Long id,
        String title,
        String teamName,
        String status,
        LocalDateTime endDate
    ) {
        /**
         * Recruit 엔티티로부터 모집 공고 요약 정보를 생성한다.
         *
         * @param recruit 모집 공고 엔티티
         * @return 모집 공고 요약 응답
         */
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
