package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.util.List;

/**
 * 시즌별 유저 수집 현황 목록 응답 DTO.
 */
public record CollectionStatusListResponse(List<CollectionStatusResponse> users) {
}
