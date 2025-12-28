package kr.ac.koreatech.sw.kosp.domain.report.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportReason;

public record ReportRequest(
    @NotNull(message = "신고 사유는 필수입니다.")
    ReportReason reason,
    String description
) {
}
