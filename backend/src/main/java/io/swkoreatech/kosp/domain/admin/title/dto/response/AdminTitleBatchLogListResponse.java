package io.swkoreatech.kosp.domain.admin.title.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.common.title.model.TitleBatchLog;

/**
 * 배치 실행 이력 목록 응답 DTO.
 *
 * @param logs        배치 이력 목록
 * @param totalCount  전체 이력 수
 * @param totalPages  전체 페이지 수
 * @param currentPage 현재 페이지 번호 (0-based)
 */
public record AdminTitleBatchLogListResponse(
    List<AdminTitleBatchLogResponse> logs,
    long totalCount,
    int totalPages,
    int currentPage
) {
    public static AdminTitleBatchLogListResponse from(Page<TitleBatchLog> page) {
        List<AdminTitleBatchLogResponse> logs = page.getContent().stream()
            .map(AdminTitleBatchLogResponse::from)
            .toList();
        return new AdminTitleBatchLogListResponse(
            logs,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber()
        );
    }
}
