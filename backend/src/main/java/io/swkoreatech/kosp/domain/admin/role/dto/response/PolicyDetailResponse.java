package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.common.auth.model.Policy;

import java.util.List;

public record PolicyDetailResponse(
    Long id,
    String name,
    String description,
    List<PermissionResponse> permissions
) {
    public static PolicyDetailResponse from(Policy policy) {
        List<PermissionResponse> permissions = policy.getPermissions().stream()
            .map(PermissionResponse::from)
            .toList();
        return new PolicyDetailResponse(
            policy.getId(),
            policy.getName(),
            policy.getDescription(),
            permissions
        );
    }
}
