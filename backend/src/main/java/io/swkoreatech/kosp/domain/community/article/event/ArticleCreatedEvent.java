package io.swkoreatech.kosp.domain.community.article.event;

import org.springframework.context.ApplicationEvent;

/**
 * 게시글 작성 이벤트.
 *
 * <p>게시글이 작성될 때 발행되며, 시즌 커뮤니티 점수 지급 등 후속 처리를 트리거한다.</p>
 */
public class ArticleCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final Long articleId;

    public ArticleCreatedEvent(Object source, Long userId, Long articleId) {
        super(source);
        this.userId = userId;
        this.articleId = articleId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getArticleId() {
        return articleId;
    }
}
