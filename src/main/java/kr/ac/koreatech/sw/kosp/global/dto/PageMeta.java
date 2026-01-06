package kr.ac.koreatech.sw.kosp.global.dto;

import org.springframework.data.domain.Page;

public record PageMeta(
    Integer currentPage,
    Integer totalPages,
    Long totalItems
) {
    public static PageMeta from(Page<?> page) {
        return new PageMeta(page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }
}
