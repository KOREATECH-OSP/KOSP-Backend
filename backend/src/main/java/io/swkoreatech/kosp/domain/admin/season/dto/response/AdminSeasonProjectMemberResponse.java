package io.swkoreatech.kosp.domain.admin.season.dto.response;

import io.swkoreatech.kosp.common.season.model.SeasonProjectMember;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectRole;

/**
 * 관리자 시즌 프로젝트 참여자 응답 DTO.
 *
 * @param memberId     참여자 레코드 ID
 * @param userId       유저 ID
 * @param userName     유저 이름
 * @param roleType     역할
 * @param roleBonus    역할 보너스
 * @param scoreGranted 점수 지급 여부
 */
public record AdminSeasonProjectMemberResponse(
    Long memberId,
    Long userId,
    String userName,
    SeasonProjectRole roleType,
    double roleBonus,
    boolean scoreGranted
) {
    public static AdminSeasonProjectMemberResponse from(SeasonProjectMember member) {
        return new AdminSeasonProjectMemberResponse(
            member.getId(),
            member.getUser().getId(),
            member.getUser().getName(),
            member.getRoleType(),
            member.getRoleType().getRoleBonus(),
            member.isScoreGranted()
        );
    }
}
