package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.domain.auth.model.Policy;

public record PolicyResponse(
    Long id,
    String name,
    String description
) {
    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
            policy.getId(),
            policy.getName(),
            policy.getDescription()
        );
    }
}
