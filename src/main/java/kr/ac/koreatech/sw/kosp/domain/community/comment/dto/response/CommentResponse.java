package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response;

import java.time.LocalDateTime;
import kr.ac.koreatech.sw.kosp.domain.community.comment.model.Comment;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.AuthorResponse;

public record CommentResponse(
    Long id,
    AuthorResponse author,
    String content,
    LocalDateTime createdAt,
    Integer likes,
    boolean isLiked,
    boolean isMine
) {
    public static CommentResponse from(Comment comment, boolean isLiked, boolean isMine) {
        return new CommentResponse(
            comment.getId(),
            AuthorResponse.from(comment.getAuthor()),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getLikes(),
            isLiked,
            isMine
        );
    }
}
