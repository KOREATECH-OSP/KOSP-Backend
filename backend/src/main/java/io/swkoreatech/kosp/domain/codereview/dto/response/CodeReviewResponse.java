package io.swkoreatech.kosp.domain.codereview.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.common.codereview.model.CodeReview;

public record CodeReviewResponse(
    Long id,
    Long authorId,
    String authorName,
    String authorProfileImage,
    String content,
    int likesCount,
    boolean likedByMe,
    LocalDateTime createdAt,
    Long parentId,
    List<CodeReviewResponse> replies
) {
    public static CodeReviewResponse of(CodeReview review, boolean likedByMe, List<CodeReviewResponse> replies) {
        String profileImage = review.getUser().getGithubUser() != null
            ? review.getUser().getGithubUser().getGithubAvatarUrl() : null;
        return new CodeReviewResponse(
            review.getId(),
            review.getUser().getId(),
            review.getUser().getName(),
            profileImage,
            review.getContent(),
            review.getLikesCount(),
            likedByMe,
            review.getCreatedAt(),
            review.getParentId(),
            replies
        );
    }
}
