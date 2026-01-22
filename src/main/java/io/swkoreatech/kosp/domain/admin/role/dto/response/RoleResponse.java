package io.swkoreatech.kosp.domain.admin.role.dto.response;

import java.util.Set;
import java.util.stream.Collectors;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;

public record RoleResponse(
    String name,
    String description,
    Boolean canAccessAdmin,
    Set<String> policies
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getName(),
            role.getDescription(),
            role.getCanAccessAdmin(),
            role.getPolicies().stream().map(Policy::getName).collect(Collectors.toSet())
        );
    }
}
