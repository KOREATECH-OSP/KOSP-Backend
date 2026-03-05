package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 팀 초대 응답 DTO.
 *
 * @param id 초대 ID
 * @param team 팀 기본 정보
 * @param inviter 초대자 정보
 * @param invitee 피초대자 정보
 * @param expiresAt 만료 시간
 * @param createdAt 생성 일시
 */
public record TeamInviteResponse(
    Long id,
    TeamBasicInfo team,
    AuthorResponse inviter,
    AuthorResponse invitee,
    Instant expiresAt,
    LocalDateTime createdAt
) {
    public record TeamBasicInfo(
        Long id,
        String name,
        String imageUrl,
        int memberCount
    ) {}

    public static TeamInviteResponse from(TeamInvite invite) {
        if (invite == null) {
            return null;
        }
        return new TeamInviteResponse(
            invite.getId(),
            new TeamBasicInfo(
                invite.getTeam().getId(),
                invite.getTeam().getName(),
                invite.getTeam().getImageUrl(),
                invite.getTeam().getMembers().size()
            ),
            AuthorResponse.from(invite.getInviter()),
            AuthorResponse.from(invite.getInvitee()),
            invite.getExpiresAt(),
            invite.getCreatedAt()
        );
    }

    public static TeamInviteResponse from(TeamInvite invite, int memberCount) {
        if (invite == null) {
            return null;
        }
        return new TeamInviteResponse(
            invite.getId(),
            new TeamBasicInfo(
                invite.getTeam().getId(),
                invite.getTeam().getName(),
                invite.getTeam().getImageUrl(),
                memberCount
            ),
            AuthorResponse.from(invite.getInviter()),
            AuthorResponse.from(invite.getInvitee()),
            invite.getExpiresAt(),
            invite.getCreatedAt()
        );
    }
}
