package io.swkoreatech.kosp.client;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RateLimitManager {

    private static final int MAX_REQUESTS_PER_HOUR = 5000;
    private static final Duration HOUR = Duration.ofHours(1);

    private final int threshold;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> windowStart = new AtomicReference<>(LocalDateTime.now());

    public RateLimitManager(@Value("${github.api.rate-limit.threshold:100}") int threshold) {
        this.threshold = threshold;
    }

    public Mono<Void> waitIfNeeded() {
        return Mono.defer(() -> {
            resetWindowIfNeeded();
            
            int current = requestCount.get();
            int remaining = MAX_REQUESTS_PER_HOUR - current;

            if (remaining <= threshold) {
                Duration waitTime = calculateWaitTime();
                log.warn("Rate limit threshold reached. Remaining: {}. Job will be rescheduled.", remaining);
                return Mono.error(new RateLimitException(
                    "Rate limit threshold reached. Remaining: " + remaining,
                    waitTime
                ));
            }

            return Mono.empty();
        });
    }

    public void recordRequest() {
        requestCount.incrementAndGet();
    }

    public Mono<Void> handleRateLimitExceeded() {
        Duration waitTime = calculateWaitTime();
        log.error("Rate limit exceeded! Waiting for: {}", waitTime);
        return Mono.delay(waitTime).then();
    }

    private void resetWindowIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = windowStart.get();

        if (Duration.between(start, now).compareTo(HOUR) >= 0) {
            windowStart.set(now);
            requestCount.set(0);
            log.info("Rate limit window reset");
        }
    }

    private Duration calculateWaitTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = windowStart.get();
        Duration elapsed = Duration.between(start, now);
        Duration remaining = HOUR.minus(elapsed);
        
        if (remaining.isNegative()) {
            return Duration.ZERO;
        }
        return remaining;
    }

    public int getRemainingRequests() {
        resetWindowIfNeeded();
        return MAX_REQUESTS_PER_HOUR - requestCount.get();
    }

    public void updateRateLimitFromHeaders(int remaining, long resetTime) {
        int used = MAX_REQUESTS_PER_HOUR - remaining;
        requestCount.set(used);
        
        LocalDateTime resetDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(resetTime),
            ZoneId.systemDefault()
        );
        windowStart.set(resetDateTime.minusHours(1));
        
        log.debug("Rate limit updated from headers: remaining={}, reset={}", 
            remaining, resetDateTime);
    }

    public Instant getResetTime() {
        LocalDateTime start = windowStart.get();
        LocalDateTime resetTime = start.plusHours(1);
        return resetTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
