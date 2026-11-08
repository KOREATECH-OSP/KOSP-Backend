package io.swkoreatech.kosp.domain.material.dto.request;

import io.swkoreatech.kosp.domain.material.model.Visibility;
import jakarta.validation.constraints.NotNull;

/**
 * 공개 범위 변경 요청 (폴더/아이템 공용).
 *
 * @param visibility 변경할 공개 범위
 */
public record VisibilityUpdateRequest(
    @NotNull Visibility visibility
) {
}
