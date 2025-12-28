package kr.ac.koreatech.sw.kosp.domain.community.team.dto.response;

import java.util.List;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;

public record TeamListResponse(
    List<TeamResponse> teams,
    PageMeta meta
) {
}
