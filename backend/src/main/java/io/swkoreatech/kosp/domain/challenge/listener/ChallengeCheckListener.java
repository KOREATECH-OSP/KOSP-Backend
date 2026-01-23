package io.swkoreatech.kosp.domain.challenge.listener;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.challenge.service.ChallengeEvaluator;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeCheckListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final UserRepository userRepository;
    private final ChallengeEvaluator challengeEvaluator;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            processMessage(message);
        } catch (Exception e) {
            log.error("Failed to process challenge check message: {}", message.getId(), e);
        }
    }

    private void processMessage(MapRecord<String, String, String> message) {
        String userIdStr = message.getValue().get("userId");
        String githubIdStr = message.getValue().get("githubId");

        log.info("Received challenge check request - userId: {}, githubId: {}", userIdStr, githubIdStr);

        if (userIdStr == null || githubIdStr == null) {
            log.warn("Invalid message format: missing userId or githubId");
            return;
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.warn("User not found: {}", userId);
            return;
        }

        challengeEvaluator.evaluate(user);
        log.info("Challenge evaluation completed for user: {}", userId);
    }
}
