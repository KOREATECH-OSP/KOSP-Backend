package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 모집 지원자 목록 응답 DTO.
 *
 * @param applications 지원 응답 목록
 * @param meta 페이징 메타 정보
 */
public record RecruitApplyListResponse(
    List<RecruitApplyResponse> applications,
    PageMeta meta
) {
    /**
     * 지원 페이지로부터 응답 객체를 생성한다.
     *
     * @param page 지원 페이지
     * @return 지원자 목록 응답
     */
    public static RecruitApplyListResponse from(Page<RecruitApply> page) {
        return new RecruitApplyListResponse(
            page.getContent().stream().map(RecruitApplyResponse::from).toList(),
            PageMeta.from(page)
        );
    }
}
