package io.swkoreatech.kosp.domain.admin.member.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

/**
 * 사용자 역할 변경 요청 DTO.
 *
 * @param roles 변경할 역할 이름 집합
 */
public record UserRoleUpdateRequest(
    @NotEmpty Set<String> roles
) {}
