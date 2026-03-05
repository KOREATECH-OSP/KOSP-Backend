package io.swkoreatech.kosp.domain.community.article.dto.response;

/**
 * 북마크 토글 응답 DTO.
 *
 * @param isBookmarked 북마크 여부
 */
public record ToggleBookmarkResponse(
    boolean isBookmarked
) {
    /**
     * 북마크 상태로부터 응답 객체를 생성한다.
     *
     * @param isBookmarked 북마크 여부
     * @return 북마크 토글 응답
     */
    public static ToggleBookmarkResponse from(boolean isBookmarked) {
        return new ToggleBookmarkResponse(isBookmarked);
    }
}
