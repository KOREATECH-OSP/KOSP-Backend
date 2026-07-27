package io.swkoreatech.kosp.common.codereview.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "code_reviews", indexes = {
    @Index(name = "idx_code_review_repo", columnList = "repo_owner, repository_name")
})
public class CodeReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_owner", nullable = false)
    private String repoOwner;

    @Column(name = "repository_name", nullable = false)
    private String repositoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "parent_id")
    private Long parentId;

    @Builder
    private CodeReview(String repoOwner, String repositoryName, User user, String content, Long parentId) {
        this.repoOwner = repoOwner;
        this.repositoryName = repositoryName;
        this.user = user;
        this.content = content;
        this.parentId = parentId;
    }

    public void incrementLikes() { this.likesCount++; }
    public void decrementLikes() { if (this.likesCount > 0) this.likesCount--; }
}
