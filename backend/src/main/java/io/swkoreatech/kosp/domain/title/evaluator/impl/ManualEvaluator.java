package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 관리자 수동 지급 전용 조건 평가기.
 *
 * <p>MANUAL 조건 유형을 가진 칭호는 자동 배치 평가 대상에서 제외되며,
 * 관리자 API를 통해서만 지급 가능하다.
 * 이 평가기는 항상 {@code false}를 반환해 배치 자동 지급을 차단한다.</p>
 */
@Component
public class ManualEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.MANUAL;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        // 수동 지급 전용 칭호는 배치 자동 평가 불가
        return false;
    }
}
