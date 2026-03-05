package io.swkoreatech.kosp.global.dto;

import org.springframework.data.domain.Page;

/**
 * 페이지 메타 정보 DTO.
 *
 * @param currentPage 현재 페이지 번호 (0-based)
 * @param totalPages  전체 페이지 수
 * @param totalItems  전체 항목 수
 */
public record PageMeta(
    Integer currentPage,
    Integer totalPages,
    Long totalItems
) {
    /**
     * {@link Page} 객체로부터 페이지 메타 정보를 생성한다.
     *
     * @param page Spring Data 페이지 객체
     * @return 페이지 메타 정보 DTO
     */
    public static PageMeta from(Page<?> page) {
        return new PageMeta(page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }
}
