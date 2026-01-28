package io.swkoreatech.kosp.domain.search.dto.response;

import io.swkoreatech.kosp.domain.user.model.User;

public record UserSummary(
    Long id,
    String name,
    String githubLogin,
    String githubName,
    String profileImageUrl
) {
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
