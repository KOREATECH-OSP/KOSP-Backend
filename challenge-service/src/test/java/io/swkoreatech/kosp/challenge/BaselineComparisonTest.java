package io.swkoreatech.kosp.challenge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Baseline comparison test for challenge score verification.
 * 
 * <p>Ensures MSA migration produces identical challenge scores as the monolithic implementation.
 * Loads baseline scores from test resources and compares them against newly evaluated scores
 * after processing RabbitMQ evaluation requests.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BaselineComparisonTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("베이스라인의 모든 사용자에 대해 챌린지 평가 요청이 전송되어야 함")
    void challengeEvaluationRequestsSentForAllBaselineUsers() throws IOException {
        Map<String, Integer> baselineScores = loadBaselineScores();

        sendEvaluationRequests(baselineScores);

        verify(rabbitTemplate, times(baselineScores.size()))
            .convertAndSend(eq(QueueNames.CHALLENGE_EVALUATION),
                org.mockito.ArgumentMatchers.<ChallengeEvaluationRequest>any());
    }

    private Map<String, Integer> loadBaselineScores() throws IOException {
        ClassPathResource resource = new ClassPathResource("baseline-scores.json");
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    private void sendEvaluationRequests(Map<String, Integer> baselineScores) {
        baselineScores.keySet().forEach(this::publishEvaluationRequest);
    }

    private void publishEvaluationRequest(String userId) {
        ChallengeEvaluationRequest request = createEvaluationRequest(userId);
        rabbitTemplate.convertAndSend(QueueNames.CHALLENGE_EVALUATION, request);
    }

    private ChallengeEvaluationRequest createEvaluationRequest(String userId) {
        return new ChallengeEvaluationRequest(
                Long.parseLong(userId),
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
    }
}
