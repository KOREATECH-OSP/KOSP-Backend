package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.domain.auth.model.Permission;

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
