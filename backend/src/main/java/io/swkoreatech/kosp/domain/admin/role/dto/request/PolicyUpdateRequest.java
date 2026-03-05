package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 정책(Policy) 수정 요청 DTO.
 *
 * @param description 수정할 정책 설명
 */
public record PolicyUpdateRequest(
    @NotBlank(message = "설명은 필수입니다")
    String description
) {
}
