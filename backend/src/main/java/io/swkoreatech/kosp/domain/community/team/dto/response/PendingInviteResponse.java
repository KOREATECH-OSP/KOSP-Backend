package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.time.Instant;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;

/**
 * 초대 대기 중인 사용자 응답 DTO.
 *
 * <p>팀원 목록에서 아직 초대를 수락하지 않은(PENDING) 사용자를 함께 표시하기 위한 DTO다.
 * 실제 팀원({@link TeamMemberResponse})과 구분하여 반투명 처리 및 초대 취소에 사용한다.</p>
 *
 * @param inviteId 초대 ID (초대 취소 시 사용)
 * @param userId 피초대자 사용자 ID
 * @param name 피초대자 이름
 * @param profileImage 피초대자 프로필 이미지 URL
 * @param expiresAt 초대 만료 시각
 */
public record PendingInviteResponse(
    Long inviteId,
    Long userId,
    String name,
    String profileImage,
    Instant expiresAt
) {
    /**
     * {@link TeamInvite} 엔티티로부터 응답을 생성한다.
     *
     * @param invite 팀 초대 엔티티 (PENDING 상태)
     * @return 초대 대기 응답
     */
    public static PendingInviteResponse from(TeamInvite invite) {
        User invitee = invite.getInvitee();
        return new PendingInviteResponse(
            invite.getId(),
            invitee.getId(),
            invitee.getName(),
            invitee.getGithubUser() != null ? invitee.getGithubUser().getGithubAvatarUrl() : null,
            invite.getExpiresAt()
        );
    }
}
