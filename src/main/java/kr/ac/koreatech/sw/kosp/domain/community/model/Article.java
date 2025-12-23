package kr.ac.koreatech.sw.kosp.domain.community.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ArticleBody body;

    @Builder
    private Article(ArticleBody body) {
        this.body = body;
    }

    public static Article create(Integer authorId, String category, String title, String content) {
        ArticleCounts counts = new ArticleCounts(0, 0);
        ArticleStats stats = new ArticleStats(0, counts);
        ArticleMeta meta = new ArticleMeta(authorId, category);
        ArticleText text = new ArticleText(title, content);
        ArticleContent articleContent = new ArticleContent(text, meta);
        ArticleBody body = new ArticleBody(articleContent, stats);

        return Article.builder()
            .body(body)
            .build();
    }

    /* Delegation Methods for Easy Access */
    public Integer getAuthorId() {
        return body.getContent().getMeta().getAuthorId();
    }

    public String getCategory() {
        return body.getContent().getMeta().getCategory();
    }

    public String getTitle() {
        return body.getContent().getText().getTitle();
    }

    public String getContent() {
        return body.getContent().getText().getBody();
    }

    public Integer getViews() {
        return body.getStats().getViews();
    }

    public Integer getLikes() {
        return body.getStats().getCounts().getLikes();
    }

    public Integer getCommentsCount() {
        return body.getStats().getCounts().getCommentsCount();
    }

    public void update(String category, String title, String content) {
        // Simplified update logic utilizing nested structure
        // In a real strict environment, we might need methods on each level
        ArticleMeta newMeta = new ArticleMeta(getAuthorId(), category);
        ArticleText newText = new ArticleText(title, content);
        ArticleContent newContent = new ArticleContent(newText, newMeta);
        
        // Use current stats
        this.body = new ArticleBody(newContent, this.body.getStats());
    }

    public void increaseViews() {
        ArticleStats currentStats = this.body.getStats();
        // Since Embeddables are often treated as value objects (immutable preferred),
        // we recreate the stats with incremented view.
        ArticleStats newStats = new ArticleStats(currentStats.getViews() + 1, currentStats.getCounts());
        this.body = new ArticleBody(this.body.getContent(), newStats);
    }
}


