package kr.ac.koreatech.sw.kosp.domain.challenge.service;

import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
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
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Transactional
    public void createChallenge(ChallengeRequest request) {
        validateSpelCondition(request.condition());

        Challenge challenge = Challenge.builder()
            .name(request.name())
            .description(request.description())
            .condition(request.condition())
            .tier(request.tier())
            .imageUrl(request.imageUrl())
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

    private void validateSpelCondition(String condition) {
        try {
            parser.parseExpression(condition);
        } catch (ParseException e) {
            log.error("Invalid SpEL condition: {}", condition, e);
            throw new GlobalException(ExceptionMessage.INVALID_CHALLENGE_CONDITION); // Need to add INVALID_CHALLENGE_CONDITION
        }
    }
}
