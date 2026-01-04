package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PolicyAssignRequest(
    @NotBlank String policyName
) {}
