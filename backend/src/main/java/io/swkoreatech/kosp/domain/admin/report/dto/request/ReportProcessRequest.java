package io.swkoreatech.kosp.domain.admin.report.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 신고 처리 요청 DTO.
 *
 * @param action 처리 작업 (콘텐츠 삭제 또는 기각)
 */
public record ReportProcessRequest(
    @NotNull(message = "처리 작업은 필수입니다.")
    Action action
) {
    /**
     * 신고 처리 작업 유형.
     */
    public enum Action {
        DELETE_CONTENT,
        REJECT
    }
}
