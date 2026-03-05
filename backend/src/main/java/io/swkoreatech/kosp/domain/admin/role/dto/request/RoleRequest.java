package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 역할(Role) 생성 요청 DTO.
 *
 * @param name           역할 이름
 * @param description    역할 설명
 * @param canAccessAdmin 관리자 페이지 접근 가능 여부
 */
public record RoleRequest(
    @NotBlank String name,
    String description,
    Boolean canAccessAdmin
) {}
