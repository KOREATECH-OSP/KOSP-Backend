package io.swkoreatech.kosp.domain.community.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;
import lombok.Builder;

@Builder
public record ArticleResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    AuthorResponse author,
    Integer views,
    Integer likes,
    Integer comments,
    List<String> tags,
    Boolean isLiked,
    Boolean isBookmarked,
    Boolean isPinned,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ArticleResponse from(Article article, boolean isLiked, boolean isBookmarked) {
        return ArticleResponse.builder()
            .id(article.getId())
            .boardId(article.getBoardId())
            .title(article.getTitle())
            .content(article.getContent())
            .author(AuthorResponse.from(article.getAuthor()))
            .views(article.getViews())
            .likes(article.getLikes())
            .comments(article.getCommentsCount())
            .tags(new java.util.ArrayList<>(article.getTags()))
            .isLiked(isLiked)
            .isBookmarked(isBookmarked)
            .isPinned(article.isPinned())
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }
}
