package io.swkoreatech.kosp.domain.report.dto.request;

import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;
import jakarta.validation.constraints.NotNull;

/**
 * 신고 요청 DTO.
 *
 * @param reason 신고 사유
 * @param description 상세 설명
 */
public record ReportRequest(
    @NotNull(message = "신고 사유는 필수입니다.")
    ReportReason reason,
    String description
) {
}
