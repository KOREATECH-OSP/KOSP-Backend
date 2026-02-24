package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;

import jakarta.validation.constraints.NotNull;

public record RecruitStatusRequest(
    @NotNull RecruitStatus status
) {
}
