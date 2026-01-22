package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.util.List;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record TeamListResponse(
    List<TeamResponse> teams,
    PageMeta meta
) {
}
