package io.swkoreatech.kosp.domain.admin.season.dto.request;

import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectRole;
import jakarta.validation.constraints.NotNull;

/**
 * 프로젝트 멤버 역할 변경 요청 DTO.
 *
 * @param roleType 변경할 역할
 */
public record AdminSeasonProjectMemberRoleChangeRequest(
    @NotNull SeasonProjectRole roleType
) {
}
