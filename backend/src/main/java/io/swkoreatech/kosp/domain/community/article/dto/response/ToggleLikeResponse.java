package io.swkoreatech.kosp.domain.community.article.dto.response;

public record ToggleLikeResponse(
    boolean isLiked
) {
    public static ToggleLikeResponse from(boolean isLiked) {
        return new ToggleLikeResponse(isLiked);
    }
}
