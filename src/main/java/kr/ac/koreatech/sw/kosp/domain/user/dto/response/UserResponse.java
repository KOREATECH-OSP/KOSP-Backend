package kr.ac.koreatech.sw.kosp.domain.user.dto.response;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record UserResponse(
    Long id,
    String email,
    String name,
    String profileImage,
    String bio
) {
    public static UserResponse from(User user) {
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;
        return new UserResponse(
            user.getId(),
            user.getKutEmail(),
            user.getName(),
            profileImage,
            null // Bio not yet implemented in User entity
        );
    }
}
