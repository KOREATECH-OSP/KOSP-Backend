package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 총 커밋 수 >= threshold 조건 평가기.
 * <p>GitHub 연동이 없는 유저는 commitCount=0이므로 자동으로 false.</p>
 */
@Component
public class CommitCountGteEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.COMMIT_COUNT_GTE;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        return context.getCommitCount() >= thresholdValue;
    }
}
