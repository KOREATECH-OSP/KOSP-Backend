package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruitment;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class RecruitListResponse {
    private final List<RecruitResponse> recruits;
    private final Integer totalPages;
    private final Long totalItems;

    private RecruitListResponse(List<RecruitResponse> recruits, Integer totalPages, Long totalItems) {
        this.recruits = recruits;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public static RecruitListResponse from(Page<Recruitment> page) {
        List<RecruitResponse> responses = page.getContent().stream()
            .map(RecruitResponse::from)
            .collect(Collectors.toList());
        return new RecruitListResponse(responses, page.getTotalPages(), page.getTotalElements());
    }
}
