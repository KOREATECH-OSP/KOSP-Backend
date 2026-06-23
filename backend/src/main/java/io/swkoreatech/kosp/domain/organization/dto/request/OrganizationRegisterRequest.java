package io.swkoreatech.kosp.domain.organization.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrganizationRegisterRequest(
    @NotNull Long githubOrgId
) {}
