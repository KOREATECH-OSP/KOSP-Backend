package io.swkoreatech.kosp.domain.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationUpdateRequest(
    @NotBlank @Size(max = 100) String displayName,
    @Size(max = 1000) String description,
    @Size(max = 500) String tags
) {
}
