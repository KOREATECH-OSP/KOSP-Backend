package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PolicyUpdateRequest(
    @NotBlank(message = "설명은 필수입니다")
    String description
) {
}
