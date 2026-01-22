package io.swkoreatech.kosp.domain.admin.point.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointTransactionRequest(
    @NotNull(message = "포인트는 필수입니다.")
    Integer point,

    @NotBlank(message = "사유는 필수입니다.")
    String reason
) {}
