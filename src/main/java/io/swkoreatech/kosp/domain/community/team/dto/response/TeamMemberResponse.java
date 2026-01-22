package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;

public record TeamMemberResponse(
    Long id,
    String name,
    String profileImage,
    TeamRole role
) {
    public static TeamMemberResponse from(TeamMember member) {
        return new TeamMemberResponse(
            member.getUser().getId(),
            member.getUser().getName(),
            member.getUser().getGithubUser() != null ? member.getUser().getGithubUser().getGithubAvatarUrl() : null,
            member.getRole()
        );
    }
}
