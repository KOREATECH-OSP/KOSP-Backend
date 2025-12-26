package kr.ac.koreatech.sw.kosp.domain.community.article.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleResponse {
    private final Long id;
    private final Long boardId;
    private final String title;
    private final String content;
    private final Integer authorId;
    private final Integer views;
    private final Integer likes;
    private final Integer comments;
    private final List<String> tags;

    Long authorId,
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
