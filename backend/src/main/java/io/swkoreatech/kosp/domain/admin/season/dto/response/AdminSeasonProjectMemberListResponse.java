package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.util.List;

import io.swkoreatech.kosp.common.season.model.SeasonProject;
import io.swkoreatech.kosp.common.season.model.SeasonProjectMember;
import io.swkoreatech.kosp.domain.admin.season.service.AdminSeasonProjectService;

/**
 * 관리자 시즌 프로젝트 참여자 목록 응답 DTO.
 *
 * @param projectId    프로젝트 ID
 * @param projectName  프로젝트 이름
 * @param projectLevel 레벨
 * @param baseScore    기본 점수
 * @param members      참여자 목록
 */
public record AdminSeasonProjectMemberListResponse(
    Long projectId,
    String projectName,
    Integer projectLevel,
    double baseScore,
    List<AdminSeasonProjectMemberResponse> members
) {
    public static AdminSeasonProjectMemberListResponse from(SeasonProject project, List<SeasonProjectMember> members) {
        return new AdminSeasonProjectMemberListResponse(
            project.getId(),
            project.getName(),
            project.getProjectLevel(),
            AdminSeasonProjectService.getBaseScore(project.getProjectLevel()),
            members.stream().map(AdminSeasonProjectMemberResponse::from).toList()
        );
    }
}
