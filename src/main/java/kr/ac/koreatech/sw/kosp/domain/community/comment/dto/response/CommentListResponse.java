package kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response;

import java.util.List;

public record CommentListResponse(
    List<CommentResponse> comments,
    boolean hasNext,
    Long lastCommentId
) {}
