package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.domain.community.team.model.Team;

import java.util.List;

/**
 * 팀 상세 응답 DTO.
 *
 * @param id 팀 ID
 * @param name 팀 이름
 * @param description 팀 설명
 * @param imageUrl 팀 이미지 URL
 * @param members 팀 멤버 목록
 */
public record TeamDetailResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    List<TeamMemberResponse> members
) {
    public static TeamDetailResponse from(Team team) {
        return new TeamDetailResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getImageUrl(),
            team.getMembers().stream().map(TeamMemberResponse::from).toList()
        );
    }
}
