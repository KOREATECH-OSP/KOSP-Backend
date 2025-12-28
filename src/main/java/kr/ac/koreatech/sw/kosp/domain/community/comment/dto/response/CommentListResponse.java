package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;

public record CommentListResponse(
    List<CommentResponse> comments,
    PageMeta meta
) {
}
