package kr.ac.koreatech.sw.kosp.domain.community.article.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Integer views = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Builder.Default
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    public static Article create(User author, Board board, String title, String content, List<String> tags) {
        return Article.builder()
            .author(author)
            .board(board)
            .title(title)
            .content(content)
            .tags(tags)
            .build();
    }

    public Long getBoardId() {
        return board.getId();
    }

    public void updateArticle(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public void increaseViews() {
        this.views++;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
