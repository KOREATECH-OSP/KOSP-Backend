package kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response;

import java.util.List;

import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;

public record RecruitApplyListResponse(
    List<RecruitApplyResponse> applications,
    PageMeta meta
) {
}
