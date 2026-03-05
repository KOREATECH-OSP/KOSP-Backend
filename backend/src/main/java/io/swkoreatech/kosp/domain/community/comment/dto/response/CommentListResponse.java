package io.swkoreatech.kosp.domain.community.comment.dto.response;

import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 댓글 목록 응답 DTO.
 *
 * @param comments 댓글 응답 목록
 * @param meta 페이징 메타 정보
 */
public record CommentListResponse(
    List<CommentResponse> comments,
    PageMeta meta
) {
    /**
     * 댓글 응답 목록과 페이지 정보로부터 응답 객체를 생성한다.
     *
     * @param comments 댓글 응답 목록
     * @param page 페이지 정보
     * @return 댓글 목록 응답
     */
    public static CommentListResponse from(List<CommentResponse> comments, Page<?> page) {
        return new CommentListResponse(comments, PageMeta.from(page));
    }
}
