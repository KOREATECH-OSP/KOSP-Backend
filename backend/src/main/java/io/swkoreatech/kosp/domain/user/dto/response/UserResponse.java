package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 사용자 응답 DTO.
 *
 * @param id 사용자 ID
 * @param name 이름
 * @param email 이메일
 * @param kutId 학번 또는 사번
 * @param profileImage 프로필 이미지 URL
 * @param introduction 자기소개
 */
public record UserResponse(
    Long id,
    String name, // Changed from email
    String email, // Changed from name
    String kutId, // Added new value
    String profileImage,
    String introduction // Changed from bio
) {
    /**
     * User 엔티티로부터 사용자 응답을 생성한다.
     *
     * @param user 사용자 엔티티
     * @return 사용자 응답
     */
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
