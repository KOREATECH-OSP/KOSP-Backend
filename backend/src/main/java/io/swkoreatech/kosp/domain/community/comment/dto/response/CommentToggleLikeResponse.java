package io.swkoreatech.kosp.domain.community.comment.dto.response;

public record CommentToggleLikeResponse(
    boolean isLiked
) {
    public static CommentToggleLikeResponse from(boolean isLiked) {
        return new CommentToggleLikeResponse(isLiked);
    }
}
