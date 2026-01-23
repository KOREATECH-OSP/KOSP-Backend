package io.swkoreatech.kosp.launcher;

import java.time.Instant;

public record JobLaunchRequest(
    Long userId,
    Priority priority,
    Instant requestedAt
) implements Comparable<JobLaunchRequest> {
    
    @Override
    public int compareTo(JobLaunchRequest other) {
        int priorityCompare = Integer.compare(this.priority.getOrder(), other.priority.getOrder());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return this.requestedAt.compareTo(other.requestedAt);
    }
}
