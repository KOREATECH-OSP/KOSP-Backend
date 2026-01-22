package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;

public record RecruitStatusRequest(
    @NotNull RecruitStatus status
) {
}
