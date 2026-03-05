package io.swkoreatech.kosp.domain.community.article.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

/**
 * 게시글 목록 응답 DTO.
 *
 * @param posts 게시글 목록
 * @param pagination 페이징 정보
 * @param <T> 게시글 응답 타입
 */
public record ArticleListResponse<T>(
    List<T> posts,
    PageMeta pagination
) {
    // Static factory removed. Service handles mapping with isLiked logic.
}
