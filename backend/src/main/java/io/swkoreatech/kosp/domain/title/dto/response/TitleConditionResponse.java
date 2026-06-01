package io.swkoreatech.kosp.domain.title.dto.response;

import io.swkoreatech.kosp.common.title.model.TitleCondition;

/**
 * 칭호 달성 조건 응답 DTO.
 *
 * @param conditionType  조건 유형 (e.g. COMMIT_COUNT_GTE)
 * @param thresholdValue 임계값
 * @param description    조건 설명 (nullable)
 */
public record TitleConditionResponse(
    String conditionType,
    int thresholdValue,
    String description
) {
    public static TitleConditionResponse from(TitleCondition condition) {
        return new TitleConditionResponse(
            condition.getConditionType().name(),
            condition.getThresholdValue(),
            condition.getDescription()
        );
    }
}
