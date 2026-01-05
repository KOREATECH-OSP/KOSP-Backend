package kr.ac.koreatech.sw.kosp.domain.github.model;

public record RateLimitInfo(
    int limit,
    int remaining,
    long resetTime  // Unix timestamp (milliseconds)
) {
    
    public boolean hasEnoughRemaining(int required) {
        return remaining >= required;
    }
    
    public long getWaitTimeMillis() {
        return Math.max(0, resetTime - System.currentTimeMillis());
    }
}

