package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;

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
