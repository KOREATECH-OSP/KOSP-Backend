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

    private static final String CHANNEL = "github_collection_trigger";

    private final JdbcTemplate jdbcTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignup(UserSignupEvent event) {
        log.info("Received UserSignupEvent for user {} (GitHub: {})",
            event.getUserId(), event.getGithubLogin());

        publishCollectionTrigger(event.getUserId());
    }

    private void publishCollectionTrigger(Long userId) {
        String payload = String.valueOf(userId);
        jdbcTemplate.execute("NOTIFY " + CHANNEL + ", '" + payload + "'");
        log.info("Published NOTIFY to channel '{}' for user {}", CHANNEL, userId);
    }
}
