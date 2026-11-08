package io.swkoreatech.kosp.domain.title.dto.response;

import java.util.List;

/**
 * 미취득 칭호 진행도 응답 DTO.
 *
 * <p>미취득·활성 칭호별로 조건별 현재값/목표값/달성률을 제공한다.
 * 여러 조건이 있는 칭호는 AND 로직이므로 전체 달성률은 조건 중 최소 달성률이다.</p>
 *
 * @param titleId 칭호 ID
 * @param titleName 칭호 이름
 * @param description 칭호 설명
 * @param category 칭호 카테고리
 * @param rarity 칭호 희소도
 * @param iconUrl 칭호 아이콘 URL
 * @param overallRate 전체 달성률(0~100). 측정 불가 조건이 포함되면 0
 * @param measurable 모든 조건이 수치 측정 가능한지 여부
 * @param conditions 조건별 진행도 목록
 */
public record TitleProgressResponse(
    Long titleId,
    String titleName,
    String description,
    String category,
    String rarity,
    String iconUrl,
    int overallRate,
    boolean measurable,
    List<ConditionProgress> conditions
) {
    /**
     * 개별 조건 진행도.
     *
     * @param conditionType 조건 유형 (COMMIT_COUNT_GTE 등)
     * @param current 현재 달성값
     * @param target 목표값(threshold)
     * @param rate 달성률(0~100)
     * @param measurable 수치 측정 가능 여부 (MANUAL 등은 false)
     */
    public record ConditionProgress(
        String conditionType,
        int current,
        int target,
        int rate,
        boolean measurable
    ) {
    }
}
