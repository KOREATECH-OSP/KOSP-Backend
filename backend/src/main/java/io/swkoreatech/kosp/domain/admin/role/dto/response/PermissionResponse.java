package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.common.auth.model.Permission;

/**
 * 권한(Permission) 응답 DTO.
 *
 * @param id          권한 식별자
 * @param name        권한 이름
 * @param description 권한 설명
 */
public record PermissionResponse(
    Long id,
    String name,
    String description
) {
    /**
     * {@link Permission} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param permission 권한 엔티티
     * @return 권한 응답 DTO
     */
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
            permission.getId(),
            permission.getName(),
            permission.getDescription()
        );
    }
}
