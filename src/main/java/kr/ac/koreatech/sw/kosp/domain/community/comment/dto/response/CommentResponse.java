package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    CommentAuthorResponse author,
    String content,
    LocalDateTime createdAt,
    Integer likes,
    boolean isLiked,
    boolean isMine
) {
    public record CommentAuthorResponse(
        Integer id,
        String name,
        String profileImage
    ) {}
}
