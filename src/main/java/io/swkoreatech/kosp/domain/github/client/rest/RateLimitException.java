package io.swkoreatech.kosp.domain.github.client.rest;

import java.time.Duration;

/**
 * Rate Limit 초과 시 발생하는 예외
 * Worker가 이 예외를 잡아서 작업을 재스케줄함
 */
public class RateLimitException extends RuntimeException {
    
    private final Duration waitTime;
    
    public RateLimitException(String message, Duration waitTime) {
        super(message);
        this.waitTime = waitTime;
    }
    
    public Duration getWaitTime() {
        return waitTime;
    }
}
