package io.swkoreatech.kosp.domain.search.dto.response;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 사용자 요약 정보 DTO.
 *
 * @param id 사용자 ID
 * @param name 사용자 이름
 * @param githubLogin GitHub 로그인명
 * @param githubName GitHub 표시 이름
 * @param profileImageUrl 프로필 이미지 URL
 */
public record UserSummary(
    Long id,
    String name,
    String githubLogin,
    String githubName,
    String profileImageUrl
) {
    /**
     * 사용자 엔티티로부터 요약 정보를 생성한다.
     *
     * @param user 사용자
     * @return 사용자 요약 정보
     */
    public static UserSummary from(User user) {
        return new UserSummary(
            user.getId(),
            user.getName(),
            extractGithubLogin(user),
            extractGithubName(user),
            extractProfileImage(user)
        );
    }

    private static String extractGithubLogin(User user) {
        if (user.getGithubUser() == null) {
            return null;
        }
        return user.getGithubUser().getGithubLogin();
    }

    private static String extractGithubName(User user) {
        if (user.getGithubUser() == null) {
            return null;
        }
        return user.getGithubUser().getGithubName();
    }

    private static String extractProfileImage(User user) {
        if (user.getGithubUser() == null) {
            return null;
        }
        return user.getGithubUser().getGithubAvatarUrl();
    }
}
