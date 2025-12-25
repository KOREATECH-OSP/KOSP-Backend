package kr.ac.koreatech.sw.kosp.domain.community.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArticleRequest {

    @NotNull(message = "게시판 ID는 필수입니다.")
    private Long boardId;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;

    public ArticleRequest(Long boardId, String title, String content, List<String> tags) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.tags = tags;
    }
}
