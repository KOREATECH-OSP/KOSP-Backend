package io.swkoreatech.kosp.domain.community.article.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 게시글 작성/수정 요청 DTO.
 *
 * @param boardId 게시판 ID
 * @param title 게시글 제목
 * @param content 게시글 내용
 * @param tags 태그 목록
 * @param attachmentIds 첨부파일 ID 목록
 */
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
