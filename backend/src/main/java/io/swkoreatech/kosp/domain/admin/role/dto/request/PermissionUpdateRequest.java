package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 권한 수정 요청 DTO.
 *
 * @param description 수정할 권한 설명
 */
public record PermissionUpdateRequest(
    @NotBlank(message = "설명은 필수입니다")
    String description
) {
}
