package io.swkoreatech.kosp.domain.community.team.dto.request;

import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import jakarta.validation.constraints.NotNull;

/**
 * 팀원 역할 변경 요청 DTO.
 *
 * <p>팀장이 특정 팀원에게 권한을 위임/회수할 때 사용한다.
 * 부여 가능한 역할은 {@code ADMIN}(관리자), {@code MEMBER}(일반 팀원)이며,
 * {@code LEADER} 위임은 별도 정책으로 허용하지 않는다.</p>
 *
 * @param role 변경할 역할
 */
public record TeamRoleUpdateRequest(
    @NotNull(message = "역할은 필수입니다.")
    TeamRole role
) {
}
