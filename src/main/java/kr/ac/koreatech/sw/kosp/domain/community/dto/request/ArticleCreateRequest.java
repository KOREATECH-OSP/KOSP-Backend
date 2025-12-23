package kr.ac.koreatech.sw.kosp.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import kr.ac.koreatech.sw.kosp.domain.community.model.Article;

public record ArticleCreateRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Content is required")
    String content,

    @NotBlank(message = "Category is required")
    String category
) {
    public Article toEntity(Integer authorId) {
        return Article.create(authorId, category, title, content);
    }
}
