package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitmentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecruitmentStatusUpdateRequest {

    @NotNull(message = "상태값은 필수입니다.")
    private RecruitmentStatus status;

    public RecruitmentStatusUpdateRequest(RecruitmentStatus status) {
        this.status = status;
    }
}
