package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 모집 공고 지원 요청 DTO.
 *
 * @param reason 지원 동기
 * @param portfolioUrl 포트폴리오 URL (선택)
 */
public record RecruitApplyRequest(
    @NotBlank(message = "지원 동기는 필수입니다.")
    String reason,

    String portfolioUrl
) {
}
