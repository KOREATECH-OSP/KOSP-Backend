package io.swkoreatech.kosp.domain.report.model.enums;

/**
 * 신고 처리 상태.
 * PENDING: 처리 대기, ACCEPTED: 신고 승인, REJECTED: 신고 반려.
 */
public enum ReportStatus {

    PENDING,    // 처리 대기
    ACCEPTED,   // 신고 승인 (제재 처리됨)
    REJECTED    // 신고 반려 (문제 없음)
}
