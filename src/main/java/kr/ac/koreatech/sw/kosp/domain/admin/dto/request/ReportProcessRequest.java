package kr.ac.koreatech.sw.kosp.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;


public record ReportProcessRequest(
    @NotNull(message = "처리 작업은 필수입니다.")
    Action action
) {
    public enum Action {
        DELETE_CONTENT,
        REJECT
    }
}
