package kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NoticeUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @NotNull(message = "상단 고정 여부는 필수입니다.")
    Boolean isPinned,

    List<String> tags
) {
}
