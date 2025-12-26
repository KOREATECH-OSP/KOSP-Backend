package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
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
