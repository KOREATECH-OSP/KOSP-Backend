package io.swkoreatech.kosp.domain.community.article.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.upload.model.Attachment;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "article")
    private List<Attachment> attachments = new ArrayList<>();

    @Builder
    protected Article(User author, Board board, String title, String content, List<String> tags, Boolean isPinned) {
        this.author = author;
        this.board = board;
        this.title = title;
        this.content = content;
        this.tags = tags != null ? tags : new ArrayList<>();
        if (isPinned != null) {
            this.isPinned = isPinned;
        }
    }

    public Long getBoardId() {
        return board.getId();
    }

    public void updateArticle(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public void updateArticle(String title, String content, Boolean isPinned, List<String> tags) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.tags = tags;
    }

    public void increaseViews() {
        this.views++;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean togglePinned() {
        this.isPinned = !this.isPinned;
        return this.isPinned;
    }

    public void incrementCommentsCount() {
        this.commentsCount++;
    }

    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }
}
