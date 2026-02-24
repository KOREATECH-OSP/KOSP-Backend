package io.swkoreatech.kosp.domain.user.eventlistener;

import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        Long userId = event.getUserId();
        log.info("UserSignupEvent for user {} (GitHub: {})", userId, event.getGithubLogin());

        try {
            GithubCollectionRequest dto = new GithubCollectionRequest(userId);
            rabbitTemplate.convertAndSend(
                QueueNames.GITHUB_COLLECTION_EXCHANGE,
                QueueNames.GITHUB_COLLECTION,
                dto,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", 0);
                    return message;
                }
            );
            log.info("Published GitHub collection request for user {}", userId);
        } catch (Exception exception) {
            log.error("Failed to publish GitHub collection request for user {}", userId, exception);
        }
    }
}
