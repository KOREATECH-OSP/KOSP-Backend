package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

public record RecruitApplyListResponse(
    List<RecruitApplyResponse> applications,
    PageMeta meta
) {
}
