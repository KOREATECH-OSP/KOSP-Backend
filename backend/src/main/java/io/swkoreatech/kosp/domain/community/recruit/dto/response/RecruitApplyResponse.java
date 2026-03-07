package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;

/**
 * 모집 지원 응답 DTO.
 *
 * @param id 지원 ID
 * @param userId 지원자 ID
 * @param userName 지원자 이름
 * @param userEmail 지원자 이메일
 * @param userProfileImage 지원자 프로필 이미지
 * @param reason 지원 동기
 * @param portfolioUrl 포트폴리오 URL
 * @param status 지원 상태
 * @param appliedAt 지원 일시
 */
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
    /**
     * 지원 엔티티로부터 응답 객체를 생성한다.
     *
     * @param apply 지원 엔티티
     * @return 지원 응답
     */
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
