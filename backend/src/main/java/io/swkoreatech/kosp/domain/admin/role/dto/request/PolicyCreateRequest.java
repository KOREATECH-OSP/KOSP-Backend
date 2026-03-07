package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 정책(Policy) 생성 요청 DTO.
 *
 * @param name        정책 이름
 * @param description 정책 설명
 */
public record PolicyCreateRequest(
    @NotBlank(message = "정책 이름은 필수입니다.")
    String name,

    String description
) {
}
