package kr.ac.koreatech.sw.kosp.domain.user.dto.response;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record AuthorResponse(
    Long id,
    String name,
    String profileImage
) {
    public static AuthorResponse from(User user) {
        if (user == null) {
            return null;
        }
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;
        return new AuthorResponse(user.getId(), user.getName(), profileImage);
    }
}
