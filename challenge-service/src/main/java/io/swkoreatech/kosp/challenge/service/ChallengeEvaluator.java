package io.swkoreatech.kosp.challenge.service;

import java.lang.reflect.Method;
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
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
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
    private final ChallengeEventPublisher challengeEventPublisher;

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

    public static int calculateProgressPercentage(int current, int target) {
        if (target <= 0) {
            return 100;
        }
        return Math.min(current * 100 / target, 100);
    }

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
