package io.swkoreatech.kosp.challenge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.challenge.publisher.ChallengeEventPublisher;
import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.common.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.common.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.common.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.common.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 챌린지 달성 여부를 평가하는 서비스.
 *
 * <p>사용자의 GitHub 활동 통계를 기반으로 등록된 모든 챌린지의 달성 조건을
 * SpEL(Spring Expression Language)로 평가하고, 달성 시 포인트를 지급한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeEvaluator {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final ChallengeEventPublisher challengeEventPublisher;

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 주어진 사용자에 대해 모든 챌린지의 달성 여부를 평가한다.
     *
     * <p>사용자의 GitHub 계정이 연동되어 있지 않거나 통계 데이터가 없으면
     * 평가를 건너뛴다. 이미 달성한 챌린지는 재평가하지 않는다.</p>
     *
     * @param user 챌린지를 평가할 대상 사용자
     */
    @Transactional
    public void evaluate(User user) {
        log.info("Starting challenge evaluation for user: {}", user.getId());

        if (user.getGithubUser() == null) {
            log.warn("No linked GitHub account for user {}", user.getId());
            return;
        }

        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        Optional<GithubUserStatistics> statsOpt = statisticsRepository.findByGithubId(githubId);

        if (statsOpt.isEmpty()) {
            log.warn("No GitHub stats found for githubId {}", githubId);
            return;
        }

        evaluateAllChallenges(user, statsOpt.get());
    }

    private void evaluateAllChallenges(User user, GithubUserStatistics stats) {
        StandardEvaluationContext context = createEvaluationContext(stats);
        List<Challenge> challenges = challengeRepository.findAll();

        challenges.forEach(challenge -> tryEvaluate(user, challenge, context));
    }

    private StandardEvaluationContext createEvaluationContext(GithubUserStatistics stats) {
        StandardEvaluationContext context = new StandardEvaluationContext(stats);
        context.setVariable("stats", stats);

        try {
            context.registerFunction("min",
                ChallengeEvaluator.class.getMethod("min", int[].class));
            context.registerFunction("max",
                ChallengeEvaluator.class.getMethod("max", int[].class));
            context.registerFunction("progress",
                ChallengeEvaluator.class.getMethod("calculateProgressPercentage", int.class, int.class));
        } catch (NoSuchMethodException e) {
            log.error("Failed to register helper functions for SpEL", e);
        }

        return context;
    }

    /**
     * 현재 값과 목표 값을 기반으로 진행률(백분율)을 계산한다.
     *
     * <p>SpEL 표현식 내에서 헬퍼 함수로 사용된다.
     * 목표 값이 0 이하이면 100%를 반환하며, 최대값은 100으로 제한된다.</p>
     *
     * @param current 현재 달성 값
     * @param target 목표 값
     * @return 0~100 범위의 진행률 (백분율)
     */
    public static int calculateProgressPercentage(int current, int target) {
        if (target <= 0) {
            return 100;
        }
        return Math.min(current * 100 / target, 100);
    }

    /**
     * 주어진 정수 배열에서 최솟값을 반환한다.
     *
     * <p>SpEL 표현식 내에서 헬퍼 함수로 사용된다.</p>
     *
     * @param values 비교할 정수 배열
     * @return 배열 내 최솟값
     * @throws IllegalArgumentException 배열이 비어 있을 경우
     */
    public static int min(int... values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("At least one value required");
        }
        int result = values[0];
        for (int i = 1; i < values.length; i++) {
            result = Math.min(result, values[i]);
        }
        return result;
    }

    /**
     * 주어진 정수 배열에서 최댓값을 반환한다.
     *
     * <p>SpEL 표현식 내에서 헬퍼 함수로 사용된다.</p>
     *
     * @param values 비교할 정수 배열
     * @return 배열 내 최댓값
     * @throws IllegalArgumentException 배열이 비어 있을 경우
     */
    public static int max(int... values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("At least one value required");
        }
        int result = values[0];
        for (int i = 1; i < values.length; i++) {
            result = Math.max(result, values[i]);
        }
        return result;
    }

    private void tryEvaluate(User user, Challenge challenge, StandardEvaluationContext context) {
        if (isAlreadyAchieved(user, challenge)) {
            return;
        }

        evaluateAndReward(user, challenge, context);
    }

    private boolean isAlreadyAchieved(User user, Challenge challenge) {
        return challengeHistoryRepository.findByUserAndChallenge(user, challenge)
            .map(ChallengeHistory::isAchieved)
            .orElse(false);
    }

    private void evaluateAndReward(User user, Challenge challenge, StandardEvaluationContext context) {
        try {
            int progress = calculateProgress(challenge, context);
            boolean isAchieved = progress >= 100;

            saveOrUpdateHistory(user, challenge, progress, isAchieved);

            if (isAchieved) {
                grantReward(user, challenge, progress);
            }
        } catch (Exception e) {
            handleEvaluationError(user, challenge, e);
        }
    }

    private int calculateProgress(Challenge challenge, StandardEvaluationContext context) {
        try {
            Expression expression = parser.parseExpression(challenge.getCondition());
            Object result = expression.getValue(context);
            return extractProgress(result);
        } catch (Exception e) {
            log.warn("Failed to calculate progress for challenge {}: {}", challenge.getId(), e.getMessage());
            return 0;
        }
    }

    private int extractProgress(Object value) {
        if (value instanceof Number number) {
            return Math.max(0, Math.min(100, number.intValue()));
        }
        return 0;
    }

    private void grantReward(User user, Challenge challenge, int progress) {
        log.info("User {} achieved challenge: {} (+{} points)",
            user.getId(), challenge.getName(), challenge.getPoint());

        String reason = String.format("챌린지 달성: %s", challenge.getName());
        challengeEventPublisher.publishPointChange(
            user.getId(),
            challenge.getPoint(),
            reason,
            "CHALLENGE"
        );

        challengeEventPublisher.publishChallengeCompleted(
            user.getId(),
            challenge.getId(),
            challenge.getName(),
            challenge.getPoint()
        );
    }

    private void handleEvaluationError(User user, Challenge challenge, Exception e) {
        log.error("Failed to evaluate challenge {} for user {}. Condition: {}",
            challenge.getId(), user.getId(), challenge.getCondition(), e);
    }

    private void saveOrUpdateHistory(User user, Challenge challenge, int progress, boolean isAchieved) {
        Optional<ChallengeHistory> existing = challengeHistoryRepository.findByUserAndChallenge(user, challenge);

        if (existing.isPresent()) {
            ChallengeHistory history = existing.get();
            history.updateProgress(progress);

            if (isAchieved && !history.isAchieved()) {
                history.achieve();
            }
        } else {
            ChallengeHistory history = ChallengeHistory.builder()
                .user(user)
                .challenge(challenge)
                .isAchieved(isAchieved)
                .achievedAt(isAchieved ? LocalDateTime.now() : null)
                .progressAtAchievement(progress)
                .build();

            challengeHistoryRepository.save(history);
        }
    }
}
