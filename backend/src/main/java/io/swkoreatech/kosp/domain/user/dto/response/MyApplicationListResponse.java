package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 본인 지원 내역 목록 응답 DTO.
 *
 * @param applications 지원 내역 목록
 * @param meta 페이지 메타 정보
 */
public record MyApplicationListResponse(
    List<MyApplicationResponse> applications,
    PageMeta meta
) {
    /**
     * 지원 내역 페이지로부터 목록 응답을 생성한다.
     *
     * @param page 지원 내역 페이지
     * @return 지원 내역 목록 응답
     */
    public static MyApplicationListResponse from(Page<RecruitApply> page) {
        return new MyApplicationListResponse(
            page.getContent().stream().map(MyApplicationResponse::from).toList(),
            PageMeta.from(page)
        );
    }
}
