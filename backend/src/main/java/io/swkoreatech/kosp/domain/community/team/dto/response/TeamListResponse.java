package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

/**
 * 팀 목록 응답 DTO.
 *
 * @param teams 팀 응답 목록
 * @param meta 페이징 메타 정보
 */
public record TeamListResponse(
    List<TeamResponse> teams,
    PageMeta meta
) {
    public static TeamListResponse from(List<TeamResponse> teams, PageMeta meta) {
        return new TeamListResponse(teams, meta);
    }
}
