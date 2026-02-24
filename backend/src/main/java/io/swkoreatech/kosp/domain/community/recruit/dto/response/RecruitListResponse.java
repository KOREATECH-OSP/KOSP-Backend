package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.util.List;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record RecruitListResponse(
    List<RecruitResponse> recruits,
    PageMeta pagination
) {
    public static RecruitListResponse from(List<RecruitResponse> recruits, PageMeta pagination) {
        return new RecruitListResponse(recruits, pagination);
    }
}
