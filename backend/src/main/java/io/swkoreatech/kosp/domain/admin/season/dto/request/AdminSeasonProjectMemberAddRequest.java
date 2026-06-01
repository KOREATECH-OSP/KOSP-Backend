package io.swkoreatech.kosp.domain.admin.season.dto.request;

import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectRole;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자 시즌 프로젝트 참여자 추가 요청 DTO.
 *
 * @param userId   추가할 유저 ID
 * @param roleType 역할 (TEAM_LEAD / PM / MEMBER)
 */
public record AdminSeasonProjectMemberAddRequest(
    @NotNull Long userId,
    @NotNull SeasonProjectRole roleType
) {
}
