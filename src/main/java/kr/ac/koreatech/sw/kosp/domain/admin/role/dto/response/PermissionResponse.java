package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;

public record PermissionResponse(
    Long id,
    String name,
    String description
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
            permission.getId(),
            permission.getName(),
            permission.getDescription()
        );
    }
}
