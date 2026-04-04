package io.swkoreatech.kosp.common.title.model.enums;

/**
 * 칭호 조건 유형.
 *
 * <p>각 조건 유형에 대응하는 {@code TitleConditionEvaluator} 구현체가 존재한다.
 * 새 조건 유형 추가 시 이 enum에 값을 추가하고 evaluator 구현체를 등록하면 된다.</p>
 */
public enum TitleConditionType {

    /** 총 커밋 수 >= threshold */
    COMMIT_COUNT_GTE,

    /** 연속 접속 일수 >= threshold */
    STREAK_DAYS_GTE,

    /** 완료한 챌린지 수 >= threshold */
    CHALLENGE_COUNT_GTE,

    /**
     * 작성한 게시글 수 >= threshold
     * TODO(2차): ArticleEventListener 기반 자동 지급 고도화 예정
     */
    ARTICLE_COUNT_GTE,

    /**
     * 팀 참여 횟수 >= threshold
     * TODO(2차): TeamJoinEventListener 기반 자동 지급 고도화 예정
     */
    TEAM_JOIN_COUNT_GTE,

    /** 관리자 수동 지급만 허용 (자동 평가 불가) */
    MANUAL
}
