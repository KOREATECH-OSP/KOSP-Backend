package io.swkoreatech.kosp.domain.community.recruit.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

/**
 * 모집 공고 목록 응답 DTO.
 *
 * @param recruits 모집 공고 응답 목록
 * @param pagination 페이징 정보
 */
public record RecruitListResponse(
    List<RecruitResponse> recruits,
    PageMeta pagination
) {
    /**
     * 모집 공고 목록과 페이징 정보로부터 응답 객체를 생성한다.
     *
     * @param recruits 모집 공고 응답 목록
     * @param pagination 페이징 메타 정보
     * @return 모집 공고 목록 응답
     */
    public static RecruitListResponse from(List<RecruitResponse> recruits, PageMeta pagination) {
        return new RecruitListResponse(recruits, pagination);
    }
}
