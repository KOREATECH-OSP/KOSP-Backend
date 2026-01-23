package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateRequest(
    @NotBlank(message = "설명은 필수입니다")
    String description,

    Boolean canAccessAdmin
) {
}
