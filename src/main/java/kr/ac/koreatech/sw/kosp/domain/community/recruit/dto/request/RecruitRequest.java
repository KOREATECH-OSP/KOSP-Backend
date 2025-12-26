package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record RecruitRequest(
    @NotNull(message = "게시판 ID는 필수입니다.")
    Long boardId,

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    List<String> tags,

    @NotNull(message = "팀 ID는 필수입니다.")
    Long teamId,

    @NotNull(message = "모집 시작일은 필수입니다.")
    LocalDateTime startDate,

    LocalDateTime endDate
) {
}
