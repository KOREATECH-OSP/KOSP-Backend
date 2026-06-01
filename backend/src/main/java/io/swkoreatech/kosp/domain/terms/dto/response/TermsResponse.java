package io.swkoreatech.kosp.domain.terms.dto.response;

import io.swkoreatech.kosp.common.terms.model.Terms;

/**
 * 약관 응답 DTO.
 *
 * @param id      약관 ID
 * @param version 약관 버전
 * @param content 약관 내용
 */
public record TermsResponse(
    Long id,
    String version,
    String content
) {
    public static TermsResponse from(Terms terms) {
        return new TermsResponse(terms.getId(), terms.getVersion(), terms.getContent());
    }
}
