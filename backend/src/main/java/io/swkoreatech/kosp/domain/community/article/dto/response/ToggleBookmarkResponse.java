package io.swkoreatech.kosp.domain.community.article.dto.response;

public record ToggleBookmarkResponse(
    boolean isBookmarked
) {
    public static ToggleBookmarkResponse from(boolean isBookmarked) {
        return new ToggleBookmarkResponse(isBookmarked);
    }
}
