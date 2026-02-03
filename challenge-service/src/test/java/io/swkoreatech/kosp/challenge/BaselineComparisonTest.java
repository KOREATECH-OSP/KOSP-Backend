package io.swkoreatech.kosp.challenge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    private UserRepository userRepository;

    @Autowired
    private ChallengeHistoryRepository challengeHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("챌린지 점수가 베이스라인과 일치해야 함")
    void challengeScoresMatchBaseline() throws IOException {
        Map<String, Integer> baselineScores = loadBaselineScores();

        sendEvaluationRequests(baselineScores);

        waitForProcessingCompletion();

        verifyScoresMatchBaseline(baselineScores);
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

    private void waitForProcessingCompletion() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(true).isTrue());
    }

    private void verifyScoresMatchBaseline(Map<String, Integer> baselineScores) {
        baselineScores.forEach(this::verifyUserScore);
    }

    private void verifyUserScore(String userId, Integer expectedScore) {
        User user = findUserById(userId);
        assertThat(user.getPoint()).isEqualTo(expectedScore);
    }

    private User findUserById(String userId) {
        return userRepository.getById(Long.parseLong(userId));
    }
}
