package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.season.model.SeasonProject;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectStatus;
import io.swkoreatech.kosp.domain.admin.season.service.AdminSeasonProjectService;

/**
 * 관리자 시즌 프로젝트 응답 DTO.
 *
 * @param id           프로젝트 ID
 * @param name         프로젝트 이름
 * @param projectLevel 레벨
 * @param baseScore    기본 점수
 * @param status       상태 (OPEN / CLOSED)
 * @param closedAt     종료 일시
 * @param note         비고
 */
public record AdminSeasonProjectResponse(
    Long id,
    String name,
    Integer projectLevel,
    double baseScore,
    SeasonProjectStatus status,
    LocalDateTime closedAt,
    String note
) {
    public static AdminSeasonProjectResponse from(SeasonProject project) {
        return new AdminSeasonProjectResponse(
            project.getId(),
            project.getName(),
            project.getProjectLevel(),
            AdminSeasonProjectService.getBaseScore(project.getProjectLevel()),
            project.getStatus(),
            project.getClosedAt(),
            project.getNote()
        );
    }
}
