package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.common.season.model.SeasonProject;

/**
 * 관리자 시즌 프로젝트 목록 응답 DTO.
 *
 * @param projects   프로젝트 목록
 * @param totalCount 전체 수
 * @param page       현재 페이지
 * @param size       페이지 크기
 */
public record AdminSeasonProjectListResponse(
    List<AdminSeasonProjectResponse> projects,
    long totalCount,
    int page,
    int size
) {
    public static AdminSeasonProjectListResponse from(Page<SeasonProject> pageResult) {
        return new AdminSeasonProjectListResponse(
            pageResult.getContent().stream().map(AdminSeasonProjectResponse::from).toList(),
            pageResult.getTotalElements(),
            pageResult.getNumber(),
            pageResult.getSize()
        );
    }
}
