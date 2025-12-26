package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;

public record RecruitListResponse(
    List<RecruitResponse> recruits,
    Integer totalPages,
    Long totalItems
) {
    public static RecruitListResponse from(Page<Recruit> page) {
        List<RecruitResponse> responses = page.getContent().stream()
            .map(RecruitResponse::from)
            .toList();
        return new RecruitListResponse(responses, page.getTotalPages(), page.getTotalElements());
    }
}
