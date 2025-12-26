package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    public CommentCreateRequest(String content) {
        this.content = content;
    }
}
