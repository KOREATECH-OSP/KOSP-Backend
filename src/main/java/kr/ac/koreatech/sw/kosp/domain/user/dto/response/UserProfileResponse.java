package kr.ac.koreatech.sw.kosp.domain.user.dto.response;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record UserProfileResponse(
    Long id,
    String name,
    String profileImage,
    String introduction,
    String githubUrl
) {
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
