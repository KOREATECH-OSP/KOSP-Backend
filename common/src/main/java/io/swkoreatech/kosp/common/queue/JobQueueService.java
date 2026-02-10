package io.swkoreatech.kosp.common.queue;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobQueueService {
    private static final String QUEUE_KEY = "job:queue";

    private final StringRedisTemplate redisTemplate;

    public void enqueue(Long userId, String runId, Instant scheduledAt, Priority priority) {
        try {
            String member = formatMember(userId, runId);
            double score = calculateScore(scheduledAt, priority);
            redisTemplate.opsForZSet().add(QUEUE_KEY, member, score);
        } catch (RedisConnectionFailureException | IllegalStateException e) {
            log.warn("Redis unavailable during enqueue: {}", e.getMessage());
        }
    }

    public Optional<JobQueueEntry> dequeue() {
        try {
            long now = Instant.now().getEpochSecond();
            var members = redisTemplate.opsForZSet()
                .rangeByScore(QUEUE_KEY, Double.NEGATIVE_INFINITY, now, 0, 1);
            if (members == null || members.isEmpty()) {
                return Optional.empty();
            }
            String member = members.iterator().next();
            redisTemplate.opsForZSet().remove(QUEUE_KEY, member);
            return Optional.of(parseMember(member));
        } catch (RedisConnectionFailureException | IllegalStateException e) {
            log.warn("Redis unavailable during dequeue: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private double calculateScore(Instant scheduledAt, Priority priority) {
        return priority.getOffset() + scheduledAt.getEpochSecond();
    }

    private String formatMember(Long userId, String runId) {
        return userId + ":" + runId;
    }

    private JobQueueEntry parseMember(String member) {
        String[] parts = member.split(":");
        return new JobQueueEntry(Long.parseLong(parts[0]), parts[1]);
    }
}
