package kr.ac.koreatech.sw.kosp.domain.community.dto.response;

import java.time.LocalDateTime;
import kr.ac.koreatech.sw.kosp.domain.community.model.Article;

public record ArticleResponse(
    Long id,
    String category,
    String title,
    String content,
    String author,
    LocalDateTime createdAt,
    Integer views,
    Integer likes,
    Integer comments,
    Integer bookmarks,
    Boolean isLiked,
    Boolean isBookmarked
) {
    public static ArticleResponse from(Article article, String authorName) {
        return new ArticleResponse(
            article.getId(),
            article.getCategory(),
            article.getTitle(),
            article.getContent(),
            authorName,
            article.getCreatedAt(),
            article.getViews(),
            article.getLikes(),
            article.getCommentsCount(),
            0, // bookmarks not implemented yet
            false, // isLiked not implemented yet
            false // isBookmarked not implemented yet
        );
    }
}
