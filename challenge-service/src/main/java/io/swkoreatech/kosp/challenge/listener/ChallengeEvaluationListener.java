package io.swkoreatech.kosp.challenge.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import io.swkoreatech.kosp.challenge.service.ChallengeEvaluator;
import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import io.swkoreatech.kosp.common.event.ChallengeEvaluationRequest;
import io.swkoreatech.kosp.common.repository.ProcessedMessageRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ를 통해 챌린지 평가 요청 메시지를 수신하는 리스너.
 *
 * <p>메시지 큐에서 {@link ChallengeEvaluationRequest}를 수신하여
 * 해당 사용자의 챌린지 달성 여부를 평가한다.
 * 멱등성을 보장하기 위해 이미 처리된 메시지는 중복 처리하지 않는다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeEvaluationListener {

    private final ChallengeEvaluator challengeEvaluator;
    private final UserRepository userRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    /**
     * 챌린지 평가 요청 메시지를 처리한다.
     *
     * <p>중복 메시지를 확인하고, 사용자를 조회한 후 챌린지 평가를 수행한다.
     * 처리 완료 시 메시지를 ACK하고, 실패 시 NACK 처리한다.
     * 동시에 최대 5개의 메시지를 병렬로 처리할 수 있다.</p>
     *
     * @param request 챌린지 평가 요청 정보 (사용자 ID, 메시지 ID 포함)
     * @param deliveryTag RabbitMQ 메시지의 배달 태그
     * @param channel RabbitMQ 채널 (ACK/NACK 응답에 사용)
     * @throws IOException 채널 ACK/NACK 전송 실패 시
     */
    @RabbitListener(queues = QueueNames.CHALLENGE_EVALUATION, concurrency = "5")
    public void handleEvaluationRequest(
        ChallengeEvaluationRequest request,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        Channel channel
    ) throws IOException {

        if (processedMessageRepository.existsByMessageId(request.messageId())) {
            log.info("Duplicate message: {}", request.messageId());
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {
            log.info("Evaluating challenges for user: {}", request.userId());

            User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));

            challengeEvaluator.evaluate(user);

            processedMessageRepository.save(
                new ProcessedMessage(request.messageId(), "ChallengeEvaluationRequest")
            );

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to evaluate challenges: userId={}", request.userId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
