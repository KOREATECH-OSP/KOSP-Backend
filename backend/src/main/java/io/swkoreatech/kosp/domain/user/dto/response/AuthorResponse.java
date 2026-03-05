package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 작성자 정보 응답 DTO.
 *
 * @param id 사용자 ID
 * @param name 이름
 * @param profileImage 프로필 이미지 URL
 */
public record AuthorResponse(
    Long id,
    String name,
    String profileImage
) {
    /**
     * User 엔티티로부터 작성자 응답을 생성한다.
     *
     * @param user 사용자 엔티티 (null인 경우 null 반환)
     * @return 작성자 응답
     */
    public static AuthorResponse from(User user) {
        if (user == null) {
            return null;
        }
        String profileImage = (user.getGithubUser() != null) ? user.getGithubUser().getGithubAvatarUrl() : null;
        return new AuthorResponse(user.getId(), user.getName(), profileImage);
    }
}
