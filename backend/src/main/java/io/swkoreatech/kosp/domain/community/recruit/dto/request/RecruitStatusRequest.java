package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 모집 상태 변경 요청 DTO.
 *
 * @param status 변경할 모집 상태
 */
public record RecruitStatusRequest(
    @NotNull RecruitStatus status
) {
}
