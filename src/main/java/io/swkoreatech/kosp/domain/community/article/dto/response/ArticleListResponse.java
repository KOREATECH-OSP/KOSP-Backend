package io.swkoreatech.kosp.domain.community.article.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

public record ArticleListResponse<T>(
    List<T> posts,
    PageMeta pagination
) {
    // Static factory removed. Service handles mapping with isLiked logic.
}
