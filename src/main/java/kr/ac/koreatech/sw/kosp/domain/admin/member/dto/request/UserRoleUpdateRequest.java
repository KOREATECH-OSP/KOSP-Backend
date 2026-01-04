package kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request;

import java.util.Set;
import jakarta.validation.constraints.NotEmpty;

public record UserRoleUpdateRequest(
    @NotEmpty Set<String> roles
) {}
