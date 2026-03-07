package io.swkoreatech.kosp.domain.community.comment.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.comment.model.Comment;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

/**
 * 댓글 응답 DTO.
 *
 * @param id 댓글 ID
 * @param author 작성자 정보
 * @param articleId 게시글 ID
 * @param articleTitle 게시글 제목
 * @param content 댓글 내용
 * @param createdAt 생성 일시
 * @param likes 좋아요 수
 * @param isLiked 현재 사용자의 좋아요 여부
 * @param isMine 현재 사용자의 작성 여부
 */
public record CommentResponse(
    Long id,
    AuthorResponse author,
    Long articleId,
    String articleTitle,
    String content,
    LocalDateTime createdAt,
    Integer likes,
    boolean isLiked,
    boolean isMine
) {
    /**
     * 댓글 엔티티로부터 응답 객체를 생성한다.
     *
     * @param comment 댓글 엔티티
     * @param isLiked 좋아요 여부
     * @param isMine 본인 작성 여부
     * @return 댓글 응답
     */
    public static CommentResponse from(Comment comment, boolean isLiked, boolean isMine) {
        return new CommentResponse(
            comment.getId(),
            AuthorResponse.from(comment.getAuthor()),
            comment.getArticle().getId(),
            comment.getArticle().getTitle(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getLikes(),
            isLiked,
            isMine
        );
    }
}
