package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;

/**
 * 팀 멤버 응답 DTO.
 *
 * @param id 사용자 ID
 * @param name 사용자 이름
 * @param profileImage 프로필 이미지 URL
 * @param role 팀 역할
 */
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
