package io.swkoreatech.kosp.domain.admin.member.dto.request;

import java.util.Set;
import jakarta.validation.constraints.NotEmpty;

public record UserRoleUpdateRequest(
    @NotEmpty Set<String> roles
) {}
