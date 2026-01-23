package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.domain.user.model.User;

public record UserResponse(
    Long id,
    String name, // Changed from email
    String email, // Changed from name
    String kutId, // Added new value
    String profileImage,
    String introduction // Changed from bio
) {
    public static UserResponse from(User user) {
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getKutEmail(),
            user.getKutId(),
            profileImage,
            user.getIntroduction()
        );
    }
}
