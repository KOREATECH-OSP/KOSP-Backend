package io.swkoreatech.kosp.domain.admin.title.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자 칭호 회수 요청 DTO.
 *
 * @param userTitleId 회수할 user_title PK
 * @param reason      회수 사유 (이력 기록용)
 */
public record AdminTitleRevokeRequest(
    @NotNull Long userTitleId,
    @NotBlank String reason
) {
}
