package kr.ac.koreatech.sw.kosp.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

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
