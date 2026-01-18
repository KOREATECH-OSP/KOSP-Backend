package kr.ac.koreatech.sw.kosp.domain.challenge.service;

import java.util.List;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.ChallengeHistory;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubProfileRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeEvaluator {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final GithubProfileRepository githubProfileRepository;
    
    // SpEL Parser is thread-safe
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 특정 사용자의 챌린지 달성 여부를 평가합니다.
     */
    @Transactional
    public void evaluate(User user) {
        log.info("Starting challenge evaluation for user: {}", user.getId());

        if (user.getGithubUser() == null) {
            log.warn("No linked GitHub account for user {}", user.getId());
            return;
        }

        Long githubId = user.getGithubUser().getGithubId();
        githubProfileRepository.findByGithubId(githubId)
            .ifPresentOrElse(
                profile -> evaluateAll(user, profile),
                () -> log.warn("No GitHub profile found for githubId {}", githubId)
            );
    }

    private void evaluateAll(User user, GithubProfile profile) {
        StandardEvaluationContext context = createEvaluationContext(profile);
        List<Challenge> challenges = challengeRepository.findAll();
        
        challenges.forEach(challenge -> tryEvaluate(user, challenge, context));
    }

    private StandardEvaluationContext createEvaluationContext(GithubProfile profile) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("activity", profile.getStats());
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
        log.info("User {} achieved challenge: {}", user.getId(), challenge.getName());
        
        ChallengeHistory history = ChallengeHistory.builder()
            .user(user)
            .challenge(challenge)
            .isAchieved(true)
            .achievedAt(java.time.LocalDateTime.now())
            .currentProgress(progress.current())
            .targetProgress(progress.target())
            .build();
            
        challengeHistoryRepository.save(history);
    }

    private void handleEvaluationError(User user, Challenge challenge, Exception e) {
        log.error("Failed to evaluate challenge {} for user {}. Condition: {}", 
            challenge.getId(), user.getId(), challenge.getCondition(), e);
    }
}
