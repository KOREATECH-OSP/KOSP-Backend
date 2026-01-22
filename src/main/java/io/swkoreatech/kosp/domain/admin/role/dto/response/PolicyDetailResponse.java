package io.swkoreatech.kosp.domain.admin.role.dto.response;

import java.util.List;

import io.swkoreatech.kosp.domain.auth.model.Policy;

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
