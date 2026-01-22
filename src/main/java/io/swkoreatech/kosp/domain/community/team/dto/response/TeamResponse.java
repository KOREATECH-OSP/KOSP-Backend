package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;
import io.swkoreatech.kosp.domain.user.model.User;

public record TeamResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    int memberCount,
    AuthorResponse createdBy
) {
    public static TeamResponse from(Team team, User leader) {
        return new TeamResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getImageUrl(),
            team.getMembers().size(),
            AuthorResponse.from(leader)
        );
    }
}
