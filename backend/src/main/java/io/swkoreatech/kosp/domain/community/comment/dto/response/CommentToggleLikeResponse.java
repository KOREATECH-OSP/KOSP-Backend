package io.swkoreatech.kosp.domain.community.comment.dto.response;

/**
 * 댓글 좋아요 토글 응답 DTO.
 *
 * @param isLiked 좋아요 여부
 */
public record CommentToggleLikeResponse(
    boolean isLiked
) {
    /**
     * 좋아요 상태로부터 응답 객체를 생성한다.
     *
     * @param isLiked 좋아요 여부
     * @return 댓글 좋아요 토글 응답
     */
    public static CommentToggleLikeResponse from(boolean isLiked) {
        return new CommentToggleLikeResponse(isLiked);
    }
}
