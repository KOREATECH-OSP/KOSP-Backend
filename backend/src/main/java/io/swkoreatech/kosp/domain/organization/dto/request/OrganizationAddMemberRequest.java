package io.swkoreatech.kosp.domain.organization.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrganizationAddMemberRequest(
    @NotBlank String githubUsername
) {
}
