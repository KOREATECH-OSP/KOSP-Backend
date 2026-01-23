package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecruitStatusUpdateRequest {

    @NotNull(message = "상태값은 필수입니다.")
    private RecruitStatus status;

    public RecruitStatusUpdateRequest(RecruitStatus status) {
        this.status = status;
    }
}
