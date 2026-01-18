package kr.ac.koreatech.sw.kosp.domain.user.dto.response;

import java.util.List;

import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;

public record MyApplicationListResponse(
    List<MyApplicationResponse> applications,
    PageMeta meta
) {
}
