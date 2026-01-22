package io.swkoreatech.kosp.domain.community.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ArticleRequest(
    @NotNull(message = "게시판 ID는 필수입니다.")
    Long boardId,

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    List<String> tags,

    List<Long> attachmentIds
) {
}
