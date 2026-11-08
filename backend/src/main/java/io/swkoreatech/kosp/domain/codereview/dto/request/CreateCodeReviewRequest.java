package io.swkoreatech.kosp.domain.codereview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCodeReviewRequest(
    @NotBlank String repoOwner,
    @NotBlank String repositoryName,
    @NotBlank @Size(max = 2000) String content,
    Long parentId,
    boolean isPrivate
) {}
