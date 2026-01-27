package io.swkoreatech.kosp.trigger;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.launcher.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionTriggerListener {

    private final JdbcTemplate jdbcTemplate;
    private final PriorityJobLauncher jobLauncher;

    @Scheduled(fixedDelay = 1000)
    public void pollTriggers() {
        List<Long> userIds = jdbcTemplate.queryForList(
            "UPDATE collection_trigger_queue " +
            "SET status = 'PROCESSING', processed_at = NOW() " +
            "WHERE status = 'PENDING' " +
            "RETURNING user_id",
            Long.class
        );

        if (userIds.isEmpty()) {
            return;
        }

        log.info("Processing {} trigger(s)", userIds.size());
        userIds.forEach(userId -> jobLauncher.submit(userId, Priority.HIGH));
    }
}
