package kr.ac.koreatech.sw.kosp.domain.github.queue.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionJob {
    
    private String jobId;
    private CollectionJobType type;
    
    // User-related fields
    private String githubLogin;
    
    // Repository-related fields
    private String repoOwner;
    private String repoName;
    
    // Authentication
    private String encryptedToken;
    
    // Job metadata
    private int priority;           // 1 = highest priority
    private int retryCount;         // Current retry count
    private int maxRetries;         // Maximum retries allowed
    
    // Timestamps
    private LocalDateTime createdAt;
    private long scheduledAt;       // Unix timestamp in milliseconds (for priority queue)
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Error tracking
    private String lastError;
    
    /**
     * 지연 실행 스케줄링
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void scheduleAfter(long delayMillis) {
        this.scheduledAt = System.currentTimeMillis() + delayMillis;
    }
    
    /**
     * 즉시 실행 스케줄링
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void scheduleNow() {
        this.scheduledAt = System.currentTimeMillis();
    }
    
    /**
     * 특정 시간에 실행 스케줄링
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void scheduleAt(long timestamp) {
        this.scheduledAt = timestamp;
    }
    
    /**
     * 실행 가능 여부 확인
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isReadyToExecute() {
        return System.currentTimeMillis() >= scheduledAt;
    }
    
    /**
     * 재시도 횟수 증가
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
