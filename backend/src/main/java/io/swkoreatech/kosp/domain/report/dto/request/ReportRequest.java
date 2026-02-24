package io.swkoreatech.kosp.domain.report.dto.request;

import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;

import jakarta.validation.constraints.NotNull;

public record ReportRequest(
    @NotNull(message = "신고 사유는 필수입니다.")
    ReportReason reason,
    String description
) {
}
