package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.model.RateLimitInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubRateLimitChecker {
    
    private final GithubRestApiClient restApiClient;
    
    /**
     * GitHub API Rate Limit 정보 조회
     */
    public RateLimitInfo checkRateLimit(String token) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restApiClient
                .getWithoutRateLimitCheck("/rate_limit", token, Map.class)
                .block();
            
            if (response == null) {
                log.warn("Rate limit response is null");
                return new RateLimitInfo(5000, 0, System.currentTimeMillis());
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> resources = (Map<String, Object>) response.get("resources");
            @SuppressWarnings("unchecked")
            Map<String, Object> core = (Map<String, Object>) resources.get("core");
            
            int limit = (int) core.get("limit");
            int remaining = (int) core.get("remaining");
            long reset = ((Number) core.get("reset")).longValue() * 1000;  // seconds to milliseconds
            
            log.debug("Rate Limit - Limit: {}, Remaining: {}, Reset: {}", 
                limit, remaining, reset);
            
            return new RateLimitInfo(limit, remaining, reset);
            
        } catch (Exception e) {
            log.error("Failed to check rate limit", e);
            // 안전하게 0으로 반환
            return new RateLimitInfo(5000, 0, System.currentTimeMillis());
        }
    }
    
    /**
     * 필요한 호출 수만큼 Rate Limit이 남아있는지 확인
     */
    public boolean hasEnoughRateLimit(String token, int required) {
        RateLimitInfo info = checkRateLimit(token);
        return info.hasEnoughRemaining(required);
    }
    
    /**
     * Rate Limit이 부족하면 Reset 시간까지 대기
     */
    public void waitIfNeeded(String token, int required) {
        RateLimitInfo info = checkRateLimit(token);
        
        if (!info.hasEnoughRemaining(required)) {
            long waitTime = info.getWaitTimeMillis();
            log.info("Rate limit insufficient. Waiting {} ms until reset", waitTime);
            
            try {
                Thread.sleep(waitTime + 1000);  // 1초 여유
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while waiting for rate limit reset", e);
            }
        }
    }
}
