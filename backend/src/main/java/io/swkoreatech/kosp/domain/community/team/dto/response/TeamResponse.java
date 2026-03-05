package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.user.dto.response.AuthorResponse;

/**
 * 팀 응답 DTO.
 *
 * @param id 팀 ID
 * @param name 팀 이름
 * @param description 팀 설명
 * @param imageUrl 팀 이미지 URL
 * @param memberCount 멤버 수
 * @param createdBy 생성자 정보
 */
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
