package io.swkoreatech.kosp.domain.title.evaluator.impl;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;

/**
 * 팀 참여 수 >= threshold 조건 평가기.
 *
 * <p>현재 배치 실행 시점의 팀 참여 수를 기준으로 평가한다.
 * TODO(2차): TeamJoinEvent 구독 방식으로 전환해 이벤트 기반 실시간 지급으로 고도화 예정.</p>
 */
@Component
public class TeamJoinCountGteEvaluator implements TitleConditionEvaluator {

    @Override
    public TitleConditionType supports() {
        return TitleConditionType.TEAM_JOIN_COUNT_GTE;
    }

    @Override
    public boolean evaluate(UserTitleContext context, int thresholdValue) {
        return context.getTeamJoinCount() >= thresholdValue;
    }

    @Override
    public int currentValue(UserTitleContext context) {
        return (int) context.getTeamJoinCount();
    }
}
