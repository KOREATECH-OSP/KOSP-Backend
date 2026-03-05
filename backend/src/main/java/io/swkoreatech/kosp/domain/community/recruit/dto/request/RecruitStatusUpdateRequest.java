package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집 상태 업데이트 요청 DTO (클래스 기반).
 */
@Getter
@NoArgsConstructor
public class RecruitStatusUpdateRequest {

    @NotNull(message = "상태값은 필수입니다.")
    private RecruitStatus status;

    public RecruitStatusUpdateRequest(RecruitStatus status) {
        this.status = status;
    }
}
