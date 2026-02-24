package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record RecruitApplyListResponse(
    List<RecruitApplyResponse> applications,
    PageMeta meta
) {
    public static RecruitApplyListResponse from(Page<RecruitApply> page) {
        return new RecruitApplyListResponse(
            page.getContent().stream().map(RecruitApplyResponse::from).toList(),
            PageMeta.from(page)
        );
    }
}
