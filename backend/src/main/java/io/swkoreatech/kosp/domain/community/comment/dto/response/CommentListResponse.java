package io.swkoreatech.kosp.domain.community.comment.dto.response;

import java.util.List;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record CommentListResponse(
    List<CommentResponse> comments,
    PageMeta meta
) {
}
