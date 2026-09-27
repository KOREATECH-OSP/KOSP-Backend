package io.swkoreatech.kosp.domain.follow.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

/**
 * 팔로워/팔로잉 목록 페이지 응답.
 *
 * @param users 사용자 목록 (최신 팔로우순)
 * @param meta  페이지 메타 정보
 */
public record FollowUserListResponse(
    List<FollowUserResponse> users,
    PageMeta meta
) {
}
