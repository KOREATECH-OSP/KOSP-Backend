package io.swkoreatech.kosp.domain.title.evaluator;

import lombok.Builder;
import lombok.Getter;

/**
 * 칭호 조건 평가에 필요한 유저 집계 컨텍스트.
 *
 * <p>{@code UserTitleContextBuilder}가 각 유저마다 생성하며,
 * {@code TitleConditionEvaluator} 구현체들이 이 컨텍스트를 참조해 조건을 판단한다.</p>
 *
 * <p>새 조건 유형 추가 시 여기에 필드를 추가하고,
 * {@code UserTitleContextBuilder}에서 값을 채워 넣으면 된다.</p>
 */
@Getter
@Builder
public class UserTitleContext {

    private final Long userId;

    /** GitHub totalCommits. GitHub 연동이 없으면 0. */
    private final int commitCount;

    /** 로그인 연속 접속 일수. streak 데이터가 없으면 0. */
    private final int loginStreakDays;

    /** isAchieved=true인 챌린지 완료 수. */
    private final int completedChallengeCount;

    /**
     * 삭제되지 않은 작성 게시글 수.
     * TODO(2차): ArticleCreatedEvent 기반 실시간 칭호 지급으로 고도화 예정
     */
    private final long articleCount;

    /**
     * 삭제되지 않은 팀 참여 수.
     * TODO(2차): TeamJoinEvent 기반 실시간 칭호 지급으로 고도화 예정
     */
    private final long teamJoinCount;
}
