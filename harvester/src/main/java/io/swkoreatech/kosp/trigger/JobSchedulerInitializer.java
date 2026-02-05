package io.swkoreatech.kosp.trigger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.common.queue.Priority;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSchedulerInitializer {

    private final UserIdProvider userIdProvider;
    private final UserRepository userRepository;
    private final JobQueueService jobQueueService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduler() {
        log.info("========== Initializing Job Scheduler ==========");
        
        List<Long> userIds = userIdProvider.findActiveUserIds();
        log.info("Found {} active users to schedule", userIds.size());
        
        int immediateCount = 0;
        int scheduledCount = 0;
        
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                continue;
            }
            if (!user.hasGithubUser()) {
                continue;
            }
            
            GithubUser githubUser = user.getGithubUser();
            Instant resetTime = githubUser.getRateLimitResetAt();
            Instant now = Instant.now();
            
            if (resetTime == null) {
                jobQueueService.enqueue(userId, UUID.randomUUID().toString(), now, Priority.HIGH);
                immediateCount++;
                continue;
            }
            if (resetTime.isBefore(now)) {
                jobQueueService.enqueue(userId, UUID.randomUUID().toString(), now, Priority.HIGH);
                immediateCount++;
                continue;
            }
            
            Instant scheduledAt = resetTime.plus(5, ChronoUnit.MINUTES);
            jobQueueService.enqueue(userId, UUID.randomUUID().toString(), scheduledAt, Priority.LOW);
            scheduledCount++;
        }
        
        log.info("========== Scheduler Initialized: {} immediate, {} scheduled ==========", 
            immediateCount, scheduledCount);
    }
}
