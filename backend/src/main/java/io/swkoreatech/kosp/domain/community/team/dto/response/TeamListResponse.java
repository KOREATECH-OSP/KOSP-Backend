package io.swkoreatech.kosp.domain.community.team.dto.response;

import io.swkoreatech.kosp.global.dto.PageMeta;

import java.util.List;

public record TeamListResponse(
    List<TeamResponse> teams,
    PageMeta meta
) {
    public static TeamListResponse from(List<TeamResponse> teams, PageMeta meta) {
        return new TeamListResponse(teams, meta);
    }
}
