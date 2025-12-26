package kr.ac.koreatech.sw.kosp.domain.community.article.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;

public record ArticleListResponse(
    List<ArticleResponse> articles,
    Integer totalPages,
    Long totalItems
) {
    public static ArticleListResponse from(Page<Article> page) {
        List<ArticleResponse> responses = page.getContent().stream()
            .map(ArticleResponse::from)
            .toList();
        return new ArticleListResponse(responses, page.getTotalPages(), page.getTotalElements());
    }
}
