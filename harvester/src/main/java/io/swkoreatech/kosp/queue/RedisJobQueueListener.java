package io.swkoreatech.kosp.queue;

import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.swkoreatech.kosp.common.queue.JobQueueEntry;
import io.swkoreatech.kosp.common.queue.JobQueueService;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisJobQueueListener {
    private final JobQueueService jobQueueService;
    private final PriorityJobLauncher jobLauncher;
    private final UserRepository userRepository;

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        Optional<JobQueueEntry> entry = jobQueueService.dequeue();
        if (entry.isEmpty()) {
            return;
        }
        processEntry(entry.get());
    }

    private void processEntry(JobQueueEntry entry) {
        if (isUserDeleted(entry.userId())) {
            log.info("Skipping job for deleted user: {}", entry.userId());
            return;
        }
        jobLauncher.run(entry.userId(), entry.runId());
    }

    private boolean isUserDeleted(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return true;
        }
        return user.isDeleted();
    }
}
