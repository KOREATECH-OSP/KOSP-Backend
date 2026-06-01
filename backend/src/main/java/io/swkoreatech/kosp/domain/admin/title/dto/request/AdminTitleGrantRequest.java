package io.swkoreatech.kosp.domain.admin.title.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자 칭호 수동 지급 요청 DTO.
 *
 * @param userId  지급 대상 유저 ID
 * @param titleId 지급할 칭호 ID
 * @param reason  지급 사유 (이력 기록용)
 */
public record AdminTitleGrantRequest(
    @NotNull Long userId,
    @NotNull Long titleId,
    @NotBlank String reason
) {
}
