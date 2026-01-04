package kr.ac.koreatech.sw.kosp.domain.community.article.dto.response;

import java.util.List;

import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;

public record ArticleListResponse(
    List<ArticleResponse> posts,
    PageMeta pagination
) {
    // Static factory removed. Service handles mapping with isLiked logic.
}
