package io.swkoreatech.kosp.domain.community.comment.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.global.dto.PageMeta;

public record CommentListResponse(
    List<CommentResponse> comments,
    PageMeta meta
) {
    public static CommentListResponse from(List<CommentResponse> comments, Page<?> page) {
        return new CommentListResponse(comments, PageMeta.from(page));
    }
}
