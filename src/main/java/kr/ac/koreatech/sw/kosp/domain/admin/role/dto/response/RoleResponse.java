package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response;

import java.util.Set;
import java.util.stream.Collectors;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;

public record RoleResponse(
    String name,
    String description,
    Set<String> policies
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
            role.getName(),
            role.getDescription(),
            role.getPolicies().stream().map(Policy::getName).collect(Collectors.toSet())
        );
    }
}
