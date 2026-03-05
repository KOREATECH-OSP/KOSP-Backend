package io.swkoreatech.kosp.client;

import java.time.Duration;

/**
 * GitHub API Rate Limit 초과 시 발생하는 예외.
 *
 * <p>대기 시간 정보를 포함하여 호출자가 적절한 재시도 전략을 수립할 수 있도록 한다.
 */
public class RateLimitException extends RuntimeException {

    private final Duration waitTime;

    /**
     * RateLimitException을 생성한다.
     *
     * @param message  예외 메시지
     * @param waitTime Rate Limit 리셋까지의 대기 시간
     */
    public RateLimitException(String message, Duration waitTime) {
        super(message);
        this.waitTime = waitTime;
    }

    /**
     * Rate Limit 리셋까지의 대기 시간을 반환한다.
     *
     * @return 대기 시간
     */
    public Duration getWaitTime() {
        return waitTime;
    }
}
