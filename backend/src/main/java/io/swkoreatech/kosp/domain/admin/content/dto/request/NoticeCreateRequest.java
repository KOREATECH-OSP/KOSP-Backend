package io.swkoreatech.kosp.domain.admin.content.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 공지사항 생성 요청 DTO.
 *
 * @param title    공지사항 제목
 * @param content  공지사항 내용
 * @param isPinned 상단 고정 여부
 * @param tags     태그 목록
 */
public record NoticeCreateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @NotNull(message = "상단 고정 여부는 필수입니다.")
    Boolean isPinned,

    List<String> tags
) {
}
