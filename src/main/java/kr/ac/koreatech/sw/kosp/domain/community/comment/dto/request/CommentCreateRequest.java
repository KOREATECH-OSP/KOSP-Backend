package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
    @NotBlank(message = "내용은 필수입니다.")
    String content
) {
}
