package io.swkoreatech.kosp.domain.title.evaluator;

import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;

/**
 * 칭호 조건 평가기 인터페이스.
 *
 * <p>새 조건 유형 추가 시 이 인터페이스를 구현하고 Spring Bean으로 등록하면,
 * {@code TitleEvaluationService}가 자동으로 발견해 사용한다.
 * 기존 코드를 수정할 필요 없다.</p>
 *
 * <pre>
 * [확장 방법]
 * 1. {@link TitleConditionType}에 새 유형 추가
 * 2. {@code TitleConditionEvaluator}를 구현하는 @Component 클래스 작성
 * 3. DB에 해당 title_condition 행 INSERT
 * </pre>
 */
public interface TitleConditionEvaluator {

    /**
     * 이 평가기가 처리하는 조건 유형을 반환한다.
     *
     * @return 처리 가능한 {@link TitleConditionType}
     */
    TitleConditionType supports();

    /**
     * 주어진 컨텍스트와 기준값으로 조건 충족 여부를 판단한다.
     *
     * @param context        유저의 집계 컨텍스트
     * @param thresholdValue 조건 기준값
     * @return 조건 충족 시 {@code true}
     */
    boolean evaluate(UserTitleContext context, int thresholdValue);

    /**
     * 미취득 칭호 진행도 표시용 현재값을 반환한다.
     *
     * <p>진행률 = min(currentValue / threshold, 1). 수치 측정이 불가능한 조건
     * (예: {@code MANUAL})은 {@code -1}을 반환한다.</p>
     *
     * @param context 유저의 집계 컨텍스트
     * @return 현재 달성값 (측정 불가 시 -1)
     */
    int currentValue(UserTitleContext context);
}
