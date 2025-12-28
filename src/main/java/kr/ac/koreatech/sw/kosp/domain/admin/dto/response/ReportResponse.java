package kr.ac.koreatech.sw.kosp.domain.admin.dto.response;

import java.time.LocalDateTime;
import kr.ac.koreatech.sw.kosp.domain.report.model.Report;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportReason;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportStatus;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportTargetType;

public record ReportResponse(
    Long id,
    Long reporterId,
    String reporterName,
    ReportTargetType targetType,
    Long targetId,
    ReportReason reason,
    String description,
    ReportStatus status,
    LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getId(),
            report.getReporter().getId(),
            report.getReporter().getName(),
            report.getTargetType(),
            report.getTargetId(),
            report.getReason(),
            report.getDescription(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }
}
