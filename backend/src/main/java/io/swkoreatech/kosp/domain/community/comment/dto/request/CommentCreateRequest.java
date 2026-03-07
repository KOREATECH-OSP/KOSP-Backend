package io.swkoreatech.kosp.domain.community.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 작성 요청 DTO.
 *
 * @param content 댓글 내용
 */
public record CommentCreateRequest(
    @NotBlank(message = "내용은 필수입니다.")
    String content
) {
}
