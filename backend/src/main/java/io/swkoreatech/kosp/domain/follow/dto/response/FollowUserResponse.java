package io.swkoreatech.kosp.domain.follow.dto.response;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 팔로우 목록의 사용자 항목 응답.
 *
 * @param userId       사용자 ID
 * @param name         이름
 * @param profileImage 프로필 이미지 URL (GitHub 아바타)
 * @param introduction 자기소개
 */
public record FollowUserResponse(
    Long userId,
    String name,
    String profileImage,
    String introduction
) {

    public static FollowUserResponse from(User user) {
        String profileImage = user.getGithubUser() != null ? user.getGithubUser().getGithubAvatarUrl() : null;
        return new FollowUserResponse(
            user.getId(),
            user.getName(),
            profileImage,
            user.getIntroduction()
        );
    }
}
