package io.swkoreatech.kosp.trigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.client.RateLimitManager;
import io.swkoreatech.kosp.common.trigger.model.CollectionTrigger;
import io.swkoreatech.kosp.common.trigger.model.TriggerPriority;
import io.swkoreatech.kosp.common.trigger.repository.CollectionTriggerRepository;
import io.swkoreatech.kosp.launcher.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionTriggerListener {

    private final CollectionTriggerRepository triggerRepository;
    private final PriorityJobLauncher jobLauncher;
    private final RateLimitManager rateLimitManager;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void pollTriggers() {
        LocalDateTime now = LocalDateTime.now();
        int claimed = triggerRepository.claimPendingTriggers(now);

        if (claimed == 0) {
            return;
        }

        List<CollectionTrigger> triggers = triggerRepository.findProcessing();
        log.info("Processing {} trigger(s)", triggers.size());

        for (CollectionTrigger trigger : triggers) {
            processTrigger(trigger);
        }
    }

    private void processTrigger(CollectionTrigger trigger) {
        try {
            Priority priority = mapPriority(trigger.getPriority());
            jobLauncher.submit(trigger.getUserId(), priority);
            trigger.complete();
            scheduleNext(trigger.getUserId());
        } catch (Exception e) {
            log.error("Failed to process trigger for user {}", trigger.getUserId(), e);
            trigger.fail();
        }
    }

    private void scheduleNext(Long userId) {
        LocalDateTime nextRun = rateLimitManager.getResetTime()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .plusMinutes(5);

        CollectionTrigger next = CollectionTrigger.createScheduled(userId, nextRun);
        triggerRepository.save(next);
        log.debug("Scheduled next collection for user {} at {}", userId, nextRun);
    }

    private Priority mapPriority(TriggerPriority triggerPriority) {
        return switch (triggerPriority) {
            case HIGH -> Priority.HIGH;
            case LOW -> Priority.LOW;
        };
    }
}
