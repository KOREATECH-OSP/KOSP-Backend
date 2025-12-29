package kr.ac.koreatech.sw.kosp.domain.community.team.dto.response;

import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;


public record TeamResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    int memberCount,
    String createdBy
) {
    public static TeamResponse from(Team team, String leaderName) {
        return new TeamResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getImageUrl(),
            team.getMembers().size(),
            leaderName
        );
    }
}
