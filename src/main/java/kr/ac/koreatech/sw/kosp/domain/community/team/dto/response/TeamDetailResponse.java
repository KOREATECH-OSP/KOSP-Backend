package kr.ac.koreatech.sw.kosp.domain.community.team.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;

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
