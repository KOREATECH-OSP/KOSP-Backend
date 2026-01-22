package io.swkoreatech.kosp.harvester.trigger;

import io.swkoreatech.kosp.harvester.launcher.Priority;
import io.swkoreatech.kosp.harvester.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicCollectionScheduler {
    
    private final PriorityJobLauncher jobLauncher;
    private final UserIdProvider userIdProvider;
    
    @Value("${harvester.scheduler.periodic-interval-hours}")
    private int intervalHours;
    
    @Scheduled(fixedRateString = "${harvester.scheduler.periodic-interval-hours:2}", timeUnit = java.util.concurrent.TimeUnit.HOURS)
    public void scheduleAllUsers() {
        List<Long> activeUserIds = userIdProvider.findActiveUserIds();
        
        log.info("Starting periodic collection for {} users (interval: {} hours)", 
            activeUserIds.size(), intervalHours);
        
        for (Long userId : activeUserIds) {
            jobLauncher.submit(userId, Priority.LOW);
        }
        
        log.info("Queued {} users for periodic collection", activeUserIds.size());
    }
}
