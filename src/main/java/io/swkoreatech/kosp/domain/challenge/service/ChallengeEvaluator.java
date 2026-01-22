package io.swkoreatech.kosp.domain.challenge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeEvaluator {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final ExpressionParser parser = new SpelExpressionParser();

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
        return context;
    }

    private void tryEvaluate(User user, Challenge challenge, StandardEvaluationContext context) {
        if (isAlreadyAchieved(user, challenge)) {
            return;
        }

        evaluateAndReward(user, challenge, context);
    }

    private boolean isAlreadyAchieved(User user, Challenge challenge) {
        return challengeHistoryRepository.existsByUserAndChallenge(user, challenge);
    }

    private void evaluateAndReward(User user, Challenge challenge, StandardEvaluationContext context) {
        try {
            ProgressInfo progress = calculateProgress(challenge, context);
            boolean isAchieved = isConditionMet(challenge, context);

            if (isAchieved) {
                grantReward(user, challenge, progress);
            }
        } catch (Exception e) {
            handleEvaluationError(user, challenge, e);
        }
    }

    private ProgressInfo calculateProgress(Challenge challenge, StandardEvaluationContext context) {
        try {
            Expression exp = parser.parseExpression(challenge.getProgressField());
            Object value = exp.getValue(context);
            int current = extractIntValue(value);
            int target = challenge.getMaxProgress();
            return new ProgressInfo(current, target);
        } catch (Exception e) {
            log.warn("Failed to calculate progress for challenge {}: {}", challenge.getId(), e.getMessage());
            return new ProgressInfo(0, challenge.getMaxProgress());
        }
    }

    private int extractIntValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private boolean isConditionMet(Challenge challenge, StandardEvaluationContext context) {
        Expression exp = parser.parseExpression(challenge.getCondition());
        return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
    }

    private void grantReward(User user, Challenge challenge, ProgressInfo progress) {
        log.info("User {} achieved challenge: {} (+{} points)", 
            user.getId(), challenge.getName(), challenge.getPoint());

        String reason = String.format("챌린지 달성: %s", challenge.getName());
        eventPublisher.publishEvent(PointChangeEvent.fromChallenge(user, challenge.getPoint(), reason));

        ChallengeHistory history = ChallengeHistory.builder()
            .user(user)
            .challenge(challenge)
            .isAchieved(true)
            .achievedAt(LocalDateTime.now())
            .currentProgress(progress.current())
            .targetProgress(progress.target())
            .build();

        challengeHistoryRepository.save(history);
    }

    private void handleEvaluationError(User user, Challenge challenge, Exception e) {
        log.error("Failed to evaluate challenge {} for user {}. Condition: {}",
            challenge.getId(), user.getId(), challenge.getCondition(), e);
    }

    private record ProgressInfo(int current, int target) {}
}
