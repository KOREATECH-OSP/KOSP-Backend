package kr.ac.koreatech.sw.kosp.domain.admin.dto.request;

import java.util.Set;
import jakarta.validation.constraints.NotEmpty;

public record UserRoleUpdateRequest(
    @NotEmpty Set<String> roles
) {}
