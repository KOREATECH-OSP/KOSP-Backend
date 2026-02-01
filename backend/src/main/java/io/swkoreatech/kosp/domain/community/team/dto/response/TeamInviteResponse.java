package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

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
                0
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
