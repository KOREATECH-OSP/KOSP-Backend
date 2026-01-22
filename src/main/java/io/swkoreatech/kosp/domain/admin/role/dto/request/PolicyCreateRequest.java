package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PolicyCreateRequest(
    @NotBlank(message = "정책 이름은 필수입니다.")
    String name,

    String description
) {
}
