package io.swkoreatech.kosp.client;

import java.time.Duration;

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
