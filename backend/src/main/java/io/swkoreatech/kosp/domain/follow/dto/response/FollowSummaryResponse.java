package io.swkoreatech.kosp.domain.follow.dto.response;

/**
 * 팔로우 요약 응답.
 *
 * @param followerCount  팔로워 수 (이 사용자를 팔로우하는 사람 수)
 * @param followingCount 팔로잉 수 (이 사용자가 팔로우하는 사람 수)
 * @param isFollowing    조회자(로그인 사용자)가 이 사용자를 팔로우 중인지 여부
 */
public record FollowSummaryResponse(
    long followerCount,
    long followingCount,
    boolean isFollowing
) {
}
