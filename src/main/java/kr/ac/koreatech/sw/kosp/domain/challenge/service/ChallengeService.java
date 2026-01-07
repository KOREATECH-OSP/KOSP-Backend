package kr.ac.koreatech.sw.kosp.domain.challenge.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest;
import kr.ac.koreatech.sw.kosp.domain.challenge.dto.response.ChallengeListResponse;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.ChallengeHistory;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
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
                challenge.getImageUrl(),
                challenge.getPoint(),
                challenge.getMaxProgress(),
                challenge.getProgressField()
            ))
            .toList();
        return new AdminChallengeListResponse(challengeInfos);
    }

    public kr.ac.koreatech.sw.kosp.domain.admin.challenge.dto.AdminChallengeResponse getChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));
        
        return new kr.ac.koreatech.sw.kosp.domain.admin.challenge.dto.AdminChallengeResponse(
            challenge.getId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getCondition(),
            challenge.getTier(),
            challenge.getImageUrl(),
            challenge.getPoint(),
            challenge.getMaxProgress(),
            challenge.getProgressField()
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
            .imageUrl(request.imageUrl())
            .point(request.point())
            .maxProgress(request.maxProgress())
            .progressField(request.progressField())
            .build();

        challengeRepository.save(challenge);
        log.info("Created challenge: {}", challenge.getName());
    }

    @Transactional
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND)); // Need to add CHALLENGE_NOT_FOUND

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
            request.imageUrl(),
            request.point(),
            request.maxProgress(),
            request.progressField()
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
        List<Challenge> challenges = (tier != null) 
            ? challengeRepository.findByTier(tier)
            : challengeRepository.findAll();
        List<ChallengeHistory> histories = challengeHistoryRepository.findAllByUserId(user.getId());
        
        Map<Long, ChallengeHistory> historyMap = histories.stream()
            .collect(Collectors.toMap(h -> h.getChallenge().getId(), h -> h));

        List<ChallengeListResponse.ChallengeResponse> challengeResponses = challenges.stream()
            .map(challenge -> {
                Optional<ChallengeHistory> historyOpt = Optional.ofNullable(historyMap.get(challenge.getId()));
                boolean isCompleted = historyOpt.map(ChallengeHistory::isAchieved).orElse(false);
                
                int current = historyOpt
                    .map(ChallengeHistory::getCurrentProgress)
                    .orElse(0);
                int total = historyOpt
                    .map(ChallengeHistory::getTargetProgress)
                    .orElse(challenge.getMaxProgress());

                return new ChallengeListResponse.ChallengeResponse(
                    challenge.getId(),
                    challenge.getName(),
                    challenge.getDescription(),
                    "general",
                    current,
                    total,
                    isCompleted,
                    challenge.getImageUrl(),
                    challenge.getTier(),
                    challenge.getPoint()
                );            })
            .toList();

        long completedCount = histories.stream().filter(ChallengeHistory::isAchieved).count();
        long totalChallenges = challenges.size();
        double overallProgress = totalChallenges > 0 
            ? (double) completedCount / totalChallenges * 100.0 
            : 0.0;
        
        int totalEarnedPoints = histories.stream()
            .filter(ChallengeHistory::isAchieved)
            .mapToInt(h -> h.getChallenge().getPoint())
            .sum();

        return new ChallengeListResponse(
            challengeResponses,
            new ChallengeListResponse.ChallengeSummary(totalChallenges, completedCount, overallProgress, totalEarnedPoints)
        );
    }
}
