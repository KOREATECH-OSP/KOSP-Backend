package io.swkoreatech.kosp.domain.community.common.dto.response;

public record LikeResponse(
    Integer likes,
    boolean isLiked
) {}
