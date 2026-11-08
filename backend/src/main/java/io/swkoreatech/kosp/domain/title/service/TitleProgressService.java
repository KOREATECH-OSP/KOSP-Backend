package io.swkoreatech.kosp.domain.title.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.TitleCondition;
import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import io.swkoreatech.kosp.common.title.repository.TitleConditionRepository;
import io.swkoreatech.kosp.common.title.repository.TitleRepository;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.title.dto.response.TitleProgressResponse;
import io.swkoreatech.kosp.domain.title.dto.response.TitleProgressResponse.ConditionProgress;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;
import lombok.RequiredArgsConstructor;

/**
 * 미취득 칭호 진행도 계산 서비스.
 *
 * <p>{@link TitleConditionEvaluator#currentValue}로 각 조건의 현재값을 얻고,
 * threshold와 비교해 달성률을 계산한다. 별도 집계 없이 기존
 * {@link UserTitleContextBuilder}의 컨텍스트를 재사용한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TitleProgressService {

    private final TitleRepository titleRepository;
    private final TitleConditionRepository titleConditionRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserTitleContextBuilder contextBuilder;
    private final List<TitleConditionEvaluator> evaluators;

    private Map<TitleConditionType, TitleConditionEvaluator> evaluatorMap;

    /**
     * 유저가 아직 취득하지 않은 활성 칭호들의 진행도를 반환한다.
     *
     * @param user 대상 유저
     * @return 미취득 칭호별 진행도 목록
     */
    public List<TitleProgressResponse> getMyProgress(User user) {
        List<Title> activeTitles = titleRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        if (activeTitles.isEmpty()) {
            return List.of();
        }

        UserTitleContext context = contextBuilder.build(user);
        List<TitleProgressResponse> result = new ArrayList<>();

        for (Title title : activeTitles) {
            // 이미 보유한 칭호는 진행도 대상에서 제외
            if (userTitleRepository.findByUserAndTitleAndIsRevokedFalse(user, title).isPresent()) {
                continue;
            }
            List<TitleCondition> conditions = titleConditionRepository.findAllByTitle(title);
            if (conditions.isEmpty()) {
                continue;
            }
            result.add(toProgress(title, conditions, context));
        }
        return result;
    }

    private TitleProgressResponse toProgress(Title title, List<TitleCondition> conditions, UserTitleContext context) {
        List<ConditionProgress> conditionProgresses = new ArrayList<>();
        boolean allMeasurable = true;
        int minRate = 100;

        for (TitleCondition condition : conditions) {
            TitleConditionEvaluator evaluator = getEvaluatorMap().get(condition.getConditionType());
            int current = evaluator == null ? -1 : evaluator.currentValue(context);
            int target = condition.getThresholdValue();
            boolean measurable = current >= 0 && target > 0;

            int rate = measurable
                ? (int) Math.min(100L, Math.round(current * 100.0 / target))
                : 0;

            if (measurable) {
                minRate = Math.min(minRate, rate);
            } else {
                allMeasurable = false;
            }

            conditionProgresses.add(new ConditionProgress(
                condition.getConditionType().name(),
                Math.max(current, 0),
                target,
                rate,
                measurable
            ));
        }

        int overallRate = allMeasurable ? minRate : 0;
        return new TitleProgressResponse(
            title.getId(),
            title.getName(),
            title.getDescription(),
            title.getCategory().name(),
            title.getRarity().name(),
            title.getIconUrl(),
            overallRate,
            allMeasurable,
            conditionProgresses
        );
    }

    private Map<TitleConditionType, TitleConditionEvaluator> getEvaluatorMap() {
        if (evaluatorMap == null) {
            evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(TitleConditionEvaluator::supports, Function.identity()));
        }
        return evaluatorMap;
    }
}
