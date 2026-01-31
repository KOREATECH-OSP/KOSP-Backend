package io.swkoreatech.kosp.challenge.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import io.swkoreatech.kosp.challenge.service.ChallengeEvaluator;
import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeEvaluationListener {
    private final ChallengeEvaluator challengeEvaluator;
    private final UserRepository userRepository;
    
    @RabbitListener(queues = QueueNames.CHALLENGE_EVALUATION, concurrency = "5")
    public void handleEvaluationRequest(
            ChallengeEvaluationRequest request,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {
        
        try {
            log.info("Evaluating challenges for user: {}", request.userId());
            
            User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));
            
            challengeEvaluator.evaluate(user);
            
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("Failed to evaluate challenges: userId={}", request.userId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
