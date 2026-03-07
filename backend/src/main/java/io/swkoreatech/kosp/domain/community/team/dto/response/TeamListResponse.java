package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

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
    /**
     * 팀 응답 목록과 페이징 메타 정보로부터 응답을 생성한다.
     *
     * @param teams 팀 응답 목록
     * @param meta  페이징 메타 정보
     * @return 팀 목록 응답
     */
    public static TeamListResponse from(List<TeamResponse> teams, PageMeta meta) {
        return new TeamListResponse(teams, meta);
    }
}
