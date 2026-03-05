package io.swkoreatech.kosp.domain.community.common.dto.response;

/**
 * 좋아요 응답 DTO.
 *
 * @param likes 좋아요 수
 * @param isLiked 현재 사용자의 좋아요 여부
 */
public record LikeResponse(
    Integer likes,
    boolean isLiked
) {
}
