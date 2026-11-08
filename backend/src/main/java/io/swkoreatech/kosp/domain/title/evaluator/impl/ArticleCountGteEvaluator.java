package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 작성 게시글 수 >= threshold 조건 평가기.
 *
 * <p>현재 배치 실행 시점의 게시글 수를 기준으로 평가한다.
 * TODO(2차): ArticleCreatedEvent 구독 방식으로 전환해 이벤트 기반 실시간 지급으로 고도화 예정.</p>
 */
@Component
public class ArticleCountGteEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.ARTICLE_COUNT_GTE;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        return context.getArticleCount() >= thresholdValue;
    }

    @Override
    public int currentValue(UserTitleContext context) {
        return (int) context.getArticleCount();
    }
}
