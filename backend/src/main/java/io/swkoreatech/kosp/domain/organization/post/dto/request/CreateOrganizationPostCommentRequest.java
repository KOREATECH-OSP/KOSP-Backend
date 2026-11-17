package io.swkoreatech.kosp.domain.organization.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationPostCommentRequest(
    @NotBlank String content
) {
}
