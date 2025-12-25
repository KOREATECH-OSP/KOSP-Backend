package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecruitRequest {

    @NotNull(message = "게시판 ID는 필수입니다.")
    private Long boardId;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;

    @NotNull(message = "팀 ID는 필수입니다.")
    private Long teamId;

    @NotNull(message = "모집 시작일은 필수입니다.")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public RecruitRequest(Long boardId, String title, String content, List<String> tags,
                          Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.teamId = teamId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
