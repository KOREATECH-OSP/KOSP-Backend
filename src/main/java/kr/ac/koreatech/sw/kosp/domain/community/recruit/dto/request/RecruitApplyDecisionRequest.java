package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;

public record RecruitApplyDecisionRequest(
    @NotNull(message = "결정 상태는 필수입니다.")
    ApplyStatus status
) {
}
