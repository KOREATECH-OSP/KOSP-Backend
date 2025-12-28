package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;

public record RecruitStatusRequest(
    @NotNull RecruitStatus status
) {
}
