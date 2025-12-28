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
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubActivity;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubActivityRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeEvaluator {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final GithubActivityRepository githubActivityRepository;
    
    // SpEL Parser is thread-safe
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 특정 사용자의 챌린지 달성 여부를 평가합니다.
     */
    @Transactional
    public void evaluate(User user) {
        log.info("Starting challenge evaluation for user: {}", user.getId());

        githubActivityRepository.findByUser(user)
            .ifPresentOrElse(
                activity -> evaluateAll(user, activity),
                () -> log.warn("No GitHub activity found for user {}", user.getId())
            );
    }

    private void evaluateAll(User user, GithubActivity activity) {
        StandardEvaluationContext context = createEvaluationContext(activity);
        List<Challenge> challenges = challengeRepository.findAll();
        
        challenges.forEach(challenge -> tryEvaluate(user, challenge, context));
    }

    private StandardEvaluationContext createEvaluationContext(GithubActivity activity) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("activity", activity.getStats());
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
            if (isConditionMet(challenge, context)) {
                grantReward(user, challenge);
            }
        } catch (Exception e) {
            handleEvaluationError(user, challenge, e);
        }
    }

    private boolean isConditionMet(Challenge challenge, StandardEvaluationContext context) {
        Expression exp = parser.parseExpression(challenge.getCondition());
        return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
    }

    private void grantReward(User user, Challenge challenge) {
        log.info("User {} achieved challenge: {}", user.getId(), challenge.getName());
        
        ChallengeHistory history = ChallengeHistory.builder()
            .user(user)
            .challenge(challenge)
            .isAchieved(true)
            .achievedAt(java.time.LocalDateTime.now())
            .build();
            
        challengeHistoryRepository.save(history);
    }

    private void handleEvaluationError(User user, Challenge challenge, Exception e) {
        log.error("Failed to evaluate challenge {} for user {}. Condition: {}", 
            challenge.getId(), user.getId(), challenge.getCondition(), e);
    }
}
