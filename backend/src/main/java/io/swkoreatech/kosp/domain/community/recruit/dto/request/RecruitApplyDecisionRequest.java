package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 지원 수락/거절 결정 요청 DTO.
 *
 * @param status 결정 상태 (ACCEPTED 또는 REJECTED)
 * @param decisionReason 결정 사유
 */
public record RecruitApplyDecisionRequest(
    @NotNull(message = "결정 상태는 필수입니다.")
    ApplyStatus status,
    
    @Size(max = 500, message = "결정 사유는 500자를 초과할 수 없습니다.")
    String decisionReason
) {
}
