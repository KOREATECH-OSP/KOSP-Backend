package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import kr.ac.koreatech.sw.kosp.domain.github.model.FailureType;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.PrematureCloseException;

@Service
@Slf4j
public class FailureAnalyzer {

    // 실패 통계 저장 (메모리 기반)
    private final Map<String, Map<FailureType, Integer>> failureStats = new ConcurrentHashMap<>();

    /**
     * 예외를 분석하여 실패 유형 분류
     */
    public FailureType classifyFailure(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        
        // Connection prematurely closed
        if (e instanceof PrematureCloseException || 
            e.getCause() instanceof PrematureCloseException ||
            message.contains("prematurely closed")) {
            return FailureType.CONNECTION_CLOSED;
        }
        
        // Timeout
        if (e instanceof TimeoutException || 
            message.contains("timeout") ||
            message.contains("timed out")) {
            return FailureType.TIMEOUT;
        }
        
        // WebClient 응답 에러
        if (e instanceof WebClientResponseException webClientError) {
            int statusCode = webClientError.getStatusCode().value();
            
            if (statusCode == 401 || statusCode == 403) {
                if (message.contains("rate limit")) {
                    return FailureType.RATE_LIMIT;
                }
                return FailureType.UNAUTHORIZED;
            } else if (statusCode == 404) {
                return FailureType.NOT_FOUND;
            } else if (statusCode >= 500) {
                return FailureType.SERVER_ERROR;
            }
        }
        
        // Network 에러
        if (e instanceof WebClientRequestException ||
            message.contains("connection") ||
            message.contains("network")) {
            return FailureType.NETWORK_ERROR;
        }
        
        return FailureType.UNKNOWN;
    }

    /**
     * 실패 기록 및 로깅
     */
    public void recordFailure(String context, FailureType type, Exception e) {
        // 통계 업데이트
        failureStats.computeIfAbsent(context, k -> new ConcurrentHashMap<>())
            .merge(type, 1, Integer::sum);
        
        // 상세 로깅
        log.error("❌ Failure in {}: {} - {}", 
            context, 
            type.getDescription(), 
            e.getMessage());
        
        // 스택 트레이스는 UNKNOWN 타입일 때만
        if (type == FailureType.UNKNOWN) {
            log.error("Unknown error details:", e);
        }
    }

    /**
     * 특정 컨텍스트의 실패 통계 조회
     */
    public Map<FailureType, Integer> getFailureStats(String context) {
        return failureStats.getOrDefault(context, new HashMap<>());
    }

    /**
     * 전체 실패 통계 로깅
     */
    public void logFailureStatistics(String context) {
        Map<FailureType, Integer> stats = getFailureStats(context);
        
        if (stats.isEmpty()) {
            log.info("✅ No failures recorded for: {}", context);
            return;
        }
        
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();
        
        log.info("📊 Failure Statistics for {}: Total {} failures", context, total);
        stats.forEach((type, count) -> {
            double percentage = (count * 100.0) / total;
            log.info("  - {}: {} ({:.1f}%)", type.getDescription(), count, percentage);
        });
        
        // 가장 많은 실패 유형 분석
        FailureType mostCommon = stats.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (mostCommon != null) {
            log.warn("⚠️ Most common failure type: {} - {}", 
                mostCommon, 
                getSuggestion(mostCommon));
        }
    }

    /**
     * 실패 유형별 해결 제안
     */
    private String getSuggestion(FailureType type) {
        return switch (type) {
            case CONNECTION_CLOSED -> 
                "Large repository issue. Consider: 1) Increase timeout, 2) Reduce page size, 3) Skip large repos";
            case TIMEOUT -> 
                "Slow response. Consider: 1) Increase timeout, 2) Retry with backoff";
            case RATE_LIMIT -> 
                "API rate limit exceeded. Consider: 1) Slow down requests, 2) Use multiple tokens";
            case UNAUTHORIZED -> 
                "Authentication issue. Check: 1) Token validity, 2) Token permissions";
            case NOT_FOUND -> 
                "Resource doesn't exist. This is expected for some cases.";
            case SERVER_ERROR -> 
                "GitHub server error. Retry later.";
            case NETWORK_ERROR -> 
                "Network connectivity issue. Check internet connection.";
            case UNKNOWN -> 
                "Unknown error. Needs investigation.";
        };
    }

    /**
     * 통계 초기화
     */
    public void clearStats(String context) {
        failureStats.remove(context);
        log.info("Cleared failure statistics for: {}", context);
    }

    /**
     * 전체 통계 초기화
     */
    public void clearAllStats() {
        failureStats.clear();
        log.info("Cleared all failure statistics");
    }
}
