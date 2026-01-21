package kr.ac.koreatech.sw.kosp.domain.github.collection.launcher;

import java.time.Instant;

/**
 * Job 실행 요청 정보.
 * PriorityBlockingQueue에서 우선순위 비교에 사용.
 */
public record JobLaunchRequest(
    Long userId,
    JobPriority priority,
    Instant requestedAt
) implements Comparable<JobLaunchRequest> {

    public static JobLaunchRequest of(Long userId, JobPriority priority) {
        return new JobLaunchRequest(userId, priority, Instant.now());
    }

    @Override
    public int compareTo(JobLaunchRequest other) {
        int priorityCompare = Integer.compare(this.priority.getOrder(), other.priority.getOrder());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return this.requestedAt.compareTo(other.requestedAt);
    }
}
