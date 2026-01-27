package io.swkoreatech.kosp.domain.user.eventlistener;

import java.time.Instant;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.queue.Priority;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final JobQueueService jobQueueService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        Long userId = event.getUserId();
        log.info("UserSignupEvent for user {} (GitHub: {})", userId, event.getGithubLogin());

        String runId = UUID.randomUUID().toString();
        jobQueueService.enqueue(userId, runId, Instant.now(), Priority.HIGH);
        log.info("Enqueued collection job for user {} with runId {}", userId, runId);
    }
}
