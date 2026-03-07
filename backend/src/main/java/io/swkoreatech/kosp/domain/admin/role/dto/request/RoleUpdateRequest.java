package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 역할(Role) 수정 요청 DTO.
 *
 * @param description    수정할 역할 설명
 * @param canAccessAdmin 관리자 페이지 접근 가능 여부
 */
public record RoleUpdateRequest(
    @NotBlank(message = "설명은 필수입니다")
    String description,

    Boolean canAccessAdmin
) {
}
