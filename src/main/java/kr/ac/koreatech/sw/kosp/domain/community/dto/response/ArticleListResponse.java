package kr.ac.koreatech.sw.kosp.domain.community.dto.response;

import java.time.LocalDateTime;
import kr.ac.koreatech.sw.kosp.domain.community.model.Article;

public record ArticleListResponse(
    Long id,
    String title,
    String category,
    String author,
    String authorProfile,
    LocalDateTime createdAt,
    Integer views,
    Integer likes,
    Integer comments,
    Integer bookmarks
) {
    public static ArticleListResponse from(Article article, String authorName, String authorProfile) {
        return new ArticleListResponse(
            article.getId(),
            article.getTitle(),
            article.getCategory(),
            authorName,
            authorProfile,
            article.getCreatedAt(),
            article.getViews(),
            article.getLikes(),
            article.getCommentsCount(),
            0 // bookmarks not implemented yet
        );
    }
}
