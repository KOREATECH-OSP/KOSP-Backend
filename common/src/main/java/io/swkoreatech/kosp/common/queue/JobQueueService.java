package io.swkoreatech.kosp.common.queue;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobQueueService {
    private static final String QUEUE_KEY = "job:queue";

    private final StringRedisTemplate redisTemplate;

    public void enqueue(Long userId, String runId, Instant scheduledAt, Priority priority) {
        String member = formatMember(userId, runId);
        double score = calculateScore(scheduledAt, priority);
        redisTemplate.opsForZSet().add(QUEUE_KEY, member, score);
    }

    public Optional<JobQueueEntry> dequeue() {
        long now = Instant.now().getEpochSecond();
        var members = redisTemplate.opsForZSet()
            .rangeByScore(QUEUE_KEY, Double.NEGATIVE_INFINITY, now, 0, 1);
        if (members == null || members.isEmpty()) {
            return Optional.empty();
        }
        String member = members.iterator().next();
        redisTemplate.opsForZSet().remove(QUEUE_KEY, member);
        return Optional.of(parseMember(member));
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
