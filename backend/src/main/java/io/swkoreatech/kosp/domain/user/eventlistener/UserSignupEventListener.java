package io.swkoreatech.kosp.domain.user.eventlistener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.common.trigger.model.CollectionTrigger;
import io.swkoreatech.kosp.common.trigger.repository.CollectionTriggerRepository;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final CollectionTriggerRepository triggerRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        Long userId = event.getUserId();
        log.info("UserSignupEvent for user {} (GitHub: {})", userId, event.getGithubLogin());

        CollectionTrigger trigger = CollectionTrigger.createImmediate(userId);
        triggerRepository.save(trigger);
        log.info("Created collection trigger for user {}", userId);
    }
}
