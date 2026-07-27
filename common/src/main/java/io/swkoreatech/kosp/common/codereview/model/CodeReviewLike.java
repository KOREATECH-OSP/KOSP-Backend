package io.swkoreatech.kosp.common.codereview.model;

import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "code_review_likes",
    uniqueConstraints = @UniqueConstraint(name = "uq_code_review_like", columnNames = {"user_id", "code_review_id"}))
public class CodeReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;

    @Builder
    private CodeReviewLike(User user, CodeReview codeReview) {
        this.user = user;
        this.codeReview = codeReview;
    }
}
