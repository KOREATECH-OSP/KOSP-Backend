package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 사용자 프로필 응답 DTO.
 *
 * @param id 사용자 ID
 * @param name 이름
 * @param profileImage 프로필 이미지 URL
 * @param introduction 자기소개
 * @param githubUrl GitHub 프로필 URL
 */
public record UserProfileResponse(
    Long id,
    String name,
    String profileImage,
    String introduction,
    String githubUrl
) {
    /**
     * User 엔티티로부터 프로필 응답을 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 프로필 응답
     */
    public static UserProfileResponse from(User user) {
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;

        String githubUrl = null;
        if (user.getGithubUser() != null && user.getGithubUser().getGithubLogin() != null) {
            githubUrl = "https://github.com/" + user.getGithubUser().getGithubLogin();
        }

        return new UserProfileResponse(
            user.getId(),
            user.getName(),
            profileImage,
            user.getIntroduction(),
            githubUrl
        );
    }
}
