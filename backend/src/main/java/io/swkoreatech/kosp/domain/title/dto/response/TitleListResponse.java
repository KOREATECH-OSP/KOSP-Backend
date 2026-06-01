package io.swkoreatech.kosp.domain.title.dto.response;

import java.util.List;

/**
 * 전체 칭호 목록 응답 DTO.
 *
 * @param titles     칭호 목록 (활성화된 칭호만, display_order 오름차순)
 * @param totalCount 총 칭호 수
 */
public record TitleListResponse(
    List<TitleDetailResponse> titles,
    int totalCount
) {}
