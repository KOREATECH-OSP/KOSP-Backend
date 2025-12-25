package kr.ac.koreatech.sw.kosp.domain.community.common.dto.response;

public record LikeResponse(
    Integer likes,
    boolean isLiked
) {}
