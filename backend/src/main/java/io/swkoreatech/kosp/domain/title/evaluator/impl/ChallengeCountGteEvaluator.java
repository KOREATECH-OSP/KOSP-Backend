package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 완료한 챌린지 수 >= threshold 조건 평가기.
 */
@Component
public class ChallengeCountGteEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.CHALLENGE_COUNT_GTE;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        return context.getCompletedChallengeCount() >= thresholdValue;
    }
}
