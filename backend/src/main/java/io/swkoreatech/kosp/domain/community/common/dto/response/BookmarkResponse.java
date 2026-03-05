package io.swkoreatech.kosp.domain.community.common.dto.response;

/**
 * 북마크 응답 DTO.
 *
 * @param isBookmarked 북마크 여부
 */
public record BookmarkResponse(
    boolean isBookmarked
) {}
