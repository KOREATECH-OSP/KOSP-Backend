package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 역할(Role) 응답 DTO.
 *
 * @param name           역할 이름
 * @param description    역할 설명
 * @param canAccessAdmin 관리자 페이지 접근 가능 여부
 * @param policies       할당된 정책 이름 집합
 */
public record RoleResponse(
    String name,
    String description,
    Boolean canAccessAdmin,
    Set<String> policies
) {
    /**
     * {@link Role} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param role 역할 엔티티
     * @return 역할 응답 DTO
     */
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getName(),
            role.getDescription(),
            role.getCanAccessAdmin(),
            role.getPolicies().stream().map(Policy::getName).collect(Collectors.toSet())
        );
    }
}
