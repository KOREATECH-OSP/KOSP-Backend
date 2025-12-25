package kr.ac.koreatech.sw.kosp.domain.community.article.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class ArticleListResponse {
    private final List<ArticleResponse> posts;
    private final Integer totalPages;
    private final Long totalItems;

    private ArticleListResponse(List<ArticleResponse> posts, Integer totalPages, Long totalItems) {
        this.posts = posts;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public static ArticleListResponse from(Page<Article> page) {
        List<ArticleResponse> responses = page.getContent().stream()
            .map(ArticleResponse::from)
            .collect(Collectors.toList());
        return new ArticleListResponse(responses, page.getTotalPages(), page.getTotalElements());
    }
}
