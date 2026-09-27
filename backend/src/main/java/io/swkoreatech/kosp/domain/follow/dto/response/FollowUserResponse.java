package io.swkoreatech.kosp.domain.follow.dto.response;

import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * 팔로우 목록의 사용자 항목 응답.
 *
 * @param userId              사용자 ID
 * @param name                이름
 * @param profileImage        프로필 이미지 URL (GitHub 아바타)
 * @param introduction        자기소개
 * @param displayTitleName    대표 칭호명 (없으면 null)
 * @param displayTitleIconUrl 대표 칭호 아이콘 URL (없으면 null)
 * @param isFollowing         조회자가 이 사용자를 팔로우 중인지 여부 (비로그인이면 false)
 * @param isMe                이 항목이 조회자 본인인지 여부 (true 면 팔로우 버튼을 노출하지 않는다)
 */
public record FollowUserResponse(
    Long userId,
    String name,
    String profileImage,
    String introduction,
    String displayTitleName,
    String displayTitleIconUrl,
    boolean isFollowing,
    boolean isMe
) {

    /**
     * 팔로우 목록 항목을 생성한다.
     *
     * @param user         목록에 표시할 사용자
     * @param displayTitle 해당 사용자의 대표 칭호 (없으면 {@code null})
     * @param isFollowing  조회자가 이 사용자를 팔로우 중인지 여부
     * @param isMe         이 사용자가 조회자 본인인지 여부
     */
    public static FollowUserResponse of(User user, UserTitle displayTitle, boolean isFollowing, boolean isMe) {
        String profileImage = user.getGithubUser() != null ? user.getGithubUser().getGithubAvatarUrl() : null;
        return new FollowUserResponse(
            user.getId(),
            user.getName(),
            profileImage,
            user.getIntroduction(),
            displayTitle == null ? null : displayTitle.getTitle().getName(),
            displayTitle == null ? null : displayTitle.getTitle().getIconUrl(),
            isFollowing,
            isMe
        );
    }
}
