package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RecruitApplyRequest(
    @NotBlank(message = "지원 동기는 필수입니다.")
    String reason,

    String portfolioUrl
) {
}
