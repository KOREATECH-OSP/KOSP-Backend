package io.swkoreatech.kosp.domain.admin.report.dto.response;

import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;

import java.time.LocalDateTime;

/**
 * 신고 응답 DTO.
 *
 * @param id           신고 식별자
 * @param reporterId   신고자 사용자 식별자
 * @param reporterName 신고자 이름
 * @param targetType   신고 대상 유형
 * @param targetId     신고 대상 식별자
 * @param reason       신고 사유
 * @param description  신고 상세 설명
 * @param status       신고 처리 상태
 * @param createdAt    신고 일시
 */
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
    /**
     * {@link Report} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param report 신고 엔티티
     * @return 신고 응답 DTO
     */
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
