package kr.ac.koreatech.sw.kosp.domain.community.article.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import lombok.Builder;

@Builder
public record ArticleResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    Long authorId,
    Integer views,
    Integer likes,
    Integer comments,
    List<String> tags
) {
    public static ArticleResponse from(Article article) {
        return ArticleResponse.builder()
            .id(article.getId())
            .boardId(article.getBoardId())
            .title(article.getTitle())
            .content(article.getContent())
            .authorId(article.getAuthorId())
            .views(article.getViews())
            .likes(article.getLikes())
            .comments(article.getCommentsCount())
            .tags(article.getTags())
            .build();
    }
}
