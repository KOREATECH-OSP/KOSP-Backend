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
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Error tracking
    private String lastError;
}
