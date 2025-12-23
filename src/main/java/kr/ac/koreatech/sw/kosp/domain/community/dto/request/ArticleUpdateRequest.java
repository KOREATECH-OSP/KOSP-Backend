package kr.ac.koreatech.sw.kosp.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ArticleUpdateRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Content is required")
    String content,

    @NotBlank(message = "Category is required")
    String category
) {
}
