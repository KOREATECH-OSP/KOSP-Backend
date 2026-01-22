package io.swkoreatech.kosp.domain.user.dto.response;

import java.util.List;

import io.swkoreatech.kosp.global.dto.PageMeta;

public record MyApplicationListResponse(
    List<MyApplicationResponse> applications,
    PageMeta meta
) {
}
