package io.swkoreatech.kosp.infra.rabbitmq.publisher;

import io.swkoreatech.kosp.common.entity.OutboxMessage;
import io.swkoreatech.kosp.common.entity.OutboxStatus;
import io.swkoreatech.kosp.common.repository.OutboxMessageRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxMessageRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pending = 
            outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        
        for (OutboxMessage message : pending) {
            try {
                String queue = mapEventTypeToQueue(message.getEventType());
                rabbitTemplate.convertAndSend(queue, message.getPayload());
                
                message.markPublished();
                outboxRepository.save(message);
                
                log.info("Published outbox: id={}, type={}", 
                    message.getId(), message.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish outbox: id={}", message.getId(), e);
                message.markFailed();
                outboxRepository.save(message);
            }
        }
    }
    
    private String mapEventTypeToQueue(String eventType) {
        return switch (eventType) {
            case "ChallengeEvaluationRequest" -> QueueNames.CHALLENGE_EVALUATION;
            case "ChallengeCompletedEvent" -> QueueNames.CHALLENGE_COMPLETED;
            case "PointChangedEvent" -> QueueNames.POINT_CHANGED;
            default -> throw new IllegalArgumentException("Unknown type: " + eventType);
        };
    }
}
