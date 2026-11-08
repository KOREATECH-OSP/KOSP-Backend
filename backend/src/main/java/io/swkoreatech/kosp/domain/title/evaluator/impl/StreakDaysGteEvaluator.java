package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 연속 접속 일수 >= threshold 조건 평가기.
 *
 * <p>streak 데이터가 아직 없는 유저는 loginStreakDays=0이므로 자동으로 false.
 * streak는 로그인 시 {@code UserLoginStreakEventListener}에 의해 갱신된다.</p>
 */
@Component
public class StreakDaysGteEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.STREAK_DAYS_GTE;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        return context.getLoginStreakDays() >= thresholdValue;
    }

    @Override
    public int currentValue(UserTitleContext context) {
        return context.getLoginStreakDays();
    }
}
