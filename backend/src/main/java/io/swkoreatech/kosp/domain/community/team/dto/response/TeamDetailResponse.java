package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.util.List;

import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;

/**
 * 팀 상세 응답 DTO.
 *
 * @param id 팀 ID
 * @param name 팀 이름
 * @param description 팀 설명
 * @param imageUrl 팀 이미지 URL
 * @param members 실제 팀원 목록 (삭제되지 않은 멤버)
 * @param pendingInvites 초대 대기 중(PENDING)인 사용자 목록
 */
public record TeamDetailResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    List<TeamMemberResponse> members,
    List<PendingInviteResponse> pendingInvites
) {
    /**
     * {@link Team} 엔티티로부터 상세 응답을 생성한다. (초대 대기자 없음)
     *
     * @param team 팀 엔티티
     * @return 팀 상세 응답
     */
    public static TeamDetailResponse from(Team team) {
        return from(team, List.of());
    }

    /**
     * {@link Team} 엔티티와 초대 대기 목록으로부터 상세 응답을 생성한다.
     *
     * @param team 팀 엔티티
     * @param pendingInvites PENDING 상태의 초대 목록
     * @return 팀 상세 응답
     */
    public static TeamDetailResponse from(Team team, List<TeamInvite> pendingInvites) {
        return new TeamDetailResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getImageUrl(),
            team.getMembers().stream()
                .filter(member -> !member.isDeleted())
                .map(TeamMemberResponse::from)
                .toList(),
            pendingInvites.stream().map(PendingInviteResponse::from).toList()
        );
    }
}
