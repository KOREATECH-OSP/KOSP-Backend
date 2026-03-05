package io.swkoreatech.kosp.domain.admin.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 포인트 변경 요청 DTO.
 *
 * @param point  변경할 포인트 (양수: 지급, 음수: 차감)
 * @param reason 포인트 변경 사유
 */
public record PointTransactionRequest(
    @NotNull(message = "포인트는 필수입니다.")
    Integer point,

    @NotBlank(message = "사유는 필수입니다.")
    String reason
) {}
