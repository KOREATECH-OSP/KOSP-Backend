package io.swkoreatech.kosp.domain.user.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record MyApplicationListResponse(
    List<MyApplicationResponse> applications,
    PageMeta meta
) {
    public static MyApplicationListResponse from(Page<RecruitApply> page) {
        return new MyApplicationListResponse(
            page.getContent().stream().map(MyApplicationResponse::from).toList(),
            PageMeta.from(page)
        );
    }
}
