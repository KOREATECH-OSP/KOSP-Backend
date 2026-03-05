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

/**
 * 게시글 엔티티.
 * 커뮤니티 게시글의 기본 정보를 관리하며, 모집글 등 하위 엔티티의 부모 클래스이다.
 */
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

    /**
     * 게시판 ID를 반환한다.
     *
     * @return 게시판 ID
     */
    public Long getBoardId() {
        return board.getId();
    }

    /**
     * 게시글 정보를 수정한다.
     *
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param tags 수정할 태그 목록
     */
    public void updateArticle(String title, String content, List<String> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    /**
     * 게시글 정보를 수정한다 (고정 여부 포함).
     *
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param isPinned 고정 여부
     * @param tags 수정할 태그 목록
     */
    public void updateArticle(String title, String content, Boolean isPinned, List<String> tags) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.tags = tags;
    }

    /** 조회수를 1 증가시킨다. */
    public void increaseViews() {
        this.views++;
    }

    /** 좋아요 수를 1 증가시킨다. */
    public void incrementLikes() {
        this.likes++;
    }

    /** 좋아요 수를 1 감소시킨다. */
    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    /** 게시글을 논리 삭제한다. */
    public void delete() {
        this.isDeleted = true;
    }

    /**
     * 고정 상태를 토글한다.
     *
     * @return 토글 후 고정 여부
     */
    public boolean togglePinned() {
        this.isPinned = !this.isPinned;
        return this.isPinned;
    }

    /** 댓글 수를 1 증가시킨다. */
    public void incrementCommentsCount() {
        this.commentsCount++;
    }

    /** 댓글 수를 1 감소시킨다. */
    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }
}
