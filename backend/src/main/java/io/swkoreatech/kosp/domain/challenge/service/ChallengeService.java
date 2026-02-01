package io.swkoreatech.kosp.domain.challenge.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeResponse;
import io.swkoreatech.kosp.domain.challenge.dto.request.ChallengeRequest;
import io.swkoreatech.kosp.domain.challenge.dto.response.ChallengeListResponse;
import io.swkoreatech.kosp.domain.challenge.dto.response.SpelVariableResponse;
import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public AdminChallengeListResponse getAllChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();
        List<AdminChallengeListResponse.ChallengeInfo> challengeInfos = challenges.stream()
            .map(challenge -> new AdminChallengeListResponse.ChallengeInfo(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                challenge.getCondition(),
                challenge.getTier(),
                challenge.getImageResource(),
                challenge.getImageResourceType(),
                challenge.getPoint()
            ))
            .toList();
        return new AdminChallengeListResponse(challengeInfos);
    }

    public AdminChallengeResponse getChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));
        
        return new AdminChallengeResponse(
            challenge.getId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getCondition(),
            challenge.getTier(),
            challenge.getImageResource(),
            challenge.getImageResourceType(),
            challenge.getPoint()
        );
    }


    @Transactional
    public void createChallenge(ChallengeRequest request) {
        validateSpelCondition(request.condition());

        Challenge challenge = Challenge.builder()
            .name(request.name())
            .description(request.description())
            .condition(request.condition())
            .tier(request.tier())
            .imageResource(request.imageResource())
            .imageResourceType(request.imageResourceType())
            .point(request.point())
            .build();

        challengeRepository.save(challenge);
        log.info("Created challenge: {}", challenge.getName());
    }

    @Transactional
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));

        challengeRepository.delete(challenge);
        log.info("Deleted challenge: {}", challengeId);
    }



    @Transactional
    public void updateChallenge(Long challengeId, ChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));

        if (!challenge.getCondition().equals(request.condition())) {
            validateSpelCondition(request.condition());
        }

        challenge.update(
            request.name(),
            request.description(),
            request.condition(),
            request.tier(),
            request.imageResource(),
            request.imageResourceType(),
            request.point()
        );
        log.info("Updated challenge: {}", challengeId);
    }

    private void validateSpelCondition(String condition) {
        try {
            parser.parseExpression(condition);
        } catch (ParseException e) {
            log.error("Invalid SpEL condition: {}", condition, e);
            throw new GlobalException(ExceptionMessage.INVALID_CHALLENGE_CONDITION); // Need to add INVALID_CHALLENGE_CONDITION
        }
    }

    public ChallengeListResponse getChallenges(User user, Integer tier) {
        List<Challenge> challenges = findChallengesByTier(tier);
        List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
        
        Map<Long, ChallengeHistory> historyMap = histories.stream()
            .collect(Collectors.toMap(h -> h.getChallenge().getId(), h -> h));

        Optional<GithubUserStatistics> statsOpt = findUserStatistics(user);
        StandardEvaluationContext context = createEvaluationContext(statsOpt.orElse(null));

        List<ChallengeListResponse.ChallengeResponse> challengeResponses = challenges.stream()
            .map(challenge -> {
                Optional<ChallengeHistory> historyOpt = Optional.ofNullable(historyMap.get(challenge.getId()));
                boolean isCompleted = historyOpt.map(ChallengeHistory::isAchieved).orElse(false);
                
                int progress = isCompleted ? 100 : evaluateProgress(challenge, context);

                return new ChallengeListResponse.ChallengeResponse(
                    challenge.getId(),
                    challenge.getName(),
                    challenge.getDescription(),
                    "general",
                    progress,
                    isCompleted,
                    challenge.getImageResource(),
                    challenge.getImageResourceType(),
                    challenge.getTier(),
                    challenge.getPoint()
                );
            })
            .toList();

        long completedCount = histories.stream().filter(ChallengeHistory::isAchieved).count();
        long totalChallenges = challenges.size();
        double overallProgress = calculateOverallProgress(completedCount, totalChallenges);
        
        int totalEarnedPoints = histories.stream()
            .filter(ChallengeHistory::isAchieved)
            .mapToInt(h -> h.getChallenge().getPoint())
            .sum();

        return new ChallengeListResponse(
            challengeResponses,
            new ChallengeListResponse.ChallengeSummary(totalChallenges, completedCount, overallProgress, totalEarnedPoints)
        );
    }

    private Optional<GithubUserStatistics> findUserStatistics(User user) {
        if (user.getGithubUser() == null) {
            return Optional.empty();
        }
        String githubId = String.valueOf(user.getGithubUser().getGithubId());
        return statisticsRepository.findByGithubId(githubId);
    }

    private StandardEvaluationContext createEvaluationContext(GithubUserStatistics stats) {
        if (stats == null) {
            return new StandardEvaluationContext();
        }
        StandardEvaluationContext context = new StandardEvaluationContext(stats);
        context.setVariable("stats", stats);
        return context;
    }

    private int evaluateProgress(Challenge challenge, StandardEvaluationContext context) {
        try {
            Expression expression = parser.parseExpression(challenge.getCondition());
            Object result = expression.getValue(context);
            return extractProgress(result);
        } catch (Exception e) {
            log.warn("Failed to evaluate condition for challenge {}: {}", challenge.getId(), e.getMessage());
            return 0;
        }
    }

    private int extractProgress(Object result) {
        if (result instanceof Number number) {
            int progress = number.intValue();
            return Math.max(0, Math.min(100, progress));
        }
        return 0;
    }

    private List<Challenge> findChallengesByTier(Integer tier) {
        if (tier == null) {
            return challengeRepository.findAll();
        }
        return challengeRepository.findByTier(tier);
    }

    private double calculateOverallProgress(long completedCount, long totalChallenges) {
        if (totalChallenges <= 0) {
            return 0.0;
        }
        return (double) completedCount / totalChallenges * 100.0;
    }

    public SpelVariableResponse getSpelVariables() {
        List<SpelVariableResponse.VariableInfo> variables = buildVariablesFromEntity();

        List<SpelVariableResponse.ExampleExpression> examples = List.of(
            new SpelVariableResponse.ExampleExpression(
                "T(Math).min(totalCommits * 100 / 100, 100)", 
                "커밋 100회 달성 (0~100%)"),
            new SpelVariableResponse.ExampleExpression(
                "T(Math).min(totalPrs * 100 / 10, 100)", 
                "PR 10개 달성 (0~100%)"),
            new SpelVariableResponse.ExampleExpression(
                "T(Math).min(totalStarsReceived * 100 / 50, 100)", 
                "스타 50개 달성 (0~100%)"),
            new SpelVariableResponse.ExampleExpression(
                "(T(Math).min(totalCommits * 100 / 50, 100) + T(Math).min(totalPrs * 100 / 5, 100)) / 2", 
                "커밋 50회 + PR 5개 복합 조건 (평균)")
        );

        return new SpelVariableResponse(variables, examples);
    }

    private List<SpelVariableResponse.VariableInfo> buildVariablesFromEntity() {
        List<SpelVariableResponse.VariableInfo> variables = new ArrayList<>();
        
        Map<String, String> descriptions = Map.ofEntries(
            Map.entry("totalCommits", "총 커밋 수"),
            Map.entry("totalLines", "총 라인 수"),
            Map.entry("totalAdditions", "총 추가 라인 수"),
            Map.entry("totalDeletions", "총 삭제 라인 수"),
            Map.entry("totalPrs", "총 PR 수"),
            Map.entry("totalIssues", "총 이슈 수"),
            Map.entry("ownedReposCount", "소유 레포지토리 수"),
            Map.entry("contributedReposCount", "기여 레포지토리 수"),
            Map.entry("totalStarsReceived", "받은 스타 수"),
            Map.entry("totalForksReceived", "받은 포크 수"),
            Map.entry("nightCommits", "야간 커밋 수 (22시~06시)"),
            Map.entry("dayCommits", "주간 커밋 수"),
            Map.entry("activityScore", "활동 점수 (0~3)"),
            Map.entry("diversityScore", "다양성 점수 (0~1)"),
            Map.entry("impactScore", "영향력 점수 (0~5)"),
            Map.entry("totalScore", "총 점수")
        );

        for (Field field : GithubUserStatistics.class.getDeclaredFields()) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            
            if (!isSpelCompatibleType(fieldType)) {
                continue;
            }

            String description = descriptions.getOrDefault(fieldName, fieldName);
            String typeName = mapToSimpleTypeName(fieldType);
            
            variables.add(new SpelVariableResponse.VariableInfo(fieldName, description, typeName));
        }
        
        return variables;
    }

    private boolean isSpelCompatibleType(Class<?> type) {
        return type == Integer.class || type == int.class 
            || type == Long.class || type == long.class
            || type == BigDecimal.class
            || type == Double.class || type == double.class;
    }

    private String mapToSimpleTypeName(Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return "Integer";
        }
        if (type == Long.class || type == long.class) {
            return "Long";
        }
        if (type == BigDecimal.class || type == Double.class || type == double.class) {
            return "Decimal";
        }
        return type.getSimpleName();
    }
}
