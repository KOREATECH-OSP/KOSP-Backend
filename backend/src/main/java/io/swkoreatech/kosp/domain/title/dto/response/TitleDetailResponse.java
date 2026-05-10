package io.swkoreatech.kosp.domain.title.dto.response;

import java.util.List;

import io.swkoreatech.kosp.common.title.model.Title;

/**
 * 칭호 상세 응답 DTO (전체 칭호 목록 조회용).
 *
 * @param id          칭호 PK
 * @param name        칭호명
 * @param description 칭호 설명
 * @param category    카테고리 (COMMIT, STREAK, CHALLENGE, ...)
 * @param rarity      희소도 (COMMON, RARE, EPIC, LEGENDARY)
 * @param iconUrl     아이콘 URL (nullable)
 * @param conditions  달성 조건 목록
 */
public record TitleDetailResponse(
    Long id,
    String name,
    String description,
    String category,
    String rarity,
    String iconUrl,
    List<TitleConditionResponse> conditions
) {
    public static TitleDetailResponse from(Title title, List<TitleConditionResponse> conditions) {
        return new TitleDetailResponse(
            title.getId(),
            title.getName(),
            title.getDescription(),
            title.getCategory().name(),
            title.getRarity().name(),
            title.getIconUrl(),
            conditions
        );
    }
}
