package io.swkoreatech.kosp.domain.community.article.dto.response;

import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

public record ArticleListResponse<T>(
    List<T> posts,
    PageMeta pagination
) {
    // Static factory removed. Service handles mapping with isLiked logic.
}
