package io.swkoreatech.kosp.domain.user.eventlistener;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupEventListener {

    private final JdbcTemplate jdbcTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        Long userId = event.getUserId();
        log.info("UserSignupEvent for user {} (GitHub: {})", userId, event.getGithubLogin());

        jdbcTemplate.update(
            "INSERT INTO collection_trigger_queue (user_id) VALUES (?)",
            userId
        );
        log.info("Queued collection trigger for user {}", userId);
    }
}
