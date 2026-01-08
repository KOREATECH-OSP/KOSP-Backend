package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.queue.dto.QueueStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CollectionJobMonitoringService {
    
    private static final String PRIORITY_QUEUE_KEY = "github:collection:priority_queue";
    private static final String PROCESSING_KEY = "github:collection:processing";
    private static final String FAILED_KEY = "github:collection:failed";
    private static final String COMPLETED_KEY = "github:collection:completed";
    
    private final RedisTemplate<String, CollectionJob> redisTemplate;
    
    public CollectionJobMonitoringService(
        @Qualifier("collectionJobRedisTemplate") RedisTemplate<String, CollectionJob> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 큐 통계 조회
     */
    public QueueStatsResponse getQueueStats() {
        Long queueLength = redisTemplate.opsForZSet().zCard(PRIORITY_QUEUE_KEY);  // Sorted Set size
        Long processingCount = (long) redisTemplate.opsForHash().size(PROCESSING_KEY);
        Long failedCount = redisTemplate.opsForList().size(FAILED_KEY);
        Long completedCount = (long) redisTemplate.opsForHash().size(COMPLETED_KEY);
        
        return QueueStatsResponse.builder()
            .queueLength(queueLength != null ? queueLength : 0L)
            .processingCount(processingCount)
            .failedCount(failedCount != null ? failedCount : 0L)
            .completedCount(completedCount)
            .build();
    }
    
    /**
     * 실패한 작업 목록 조회
     */
    public List<CollectionJob> getFailedJobs() {
        Long size = redisTemplate.opsForList().size(FAILED_KEY);
        if (size == null || size == 0) {
            return List.of();
        }
        
        List<CollectionJob> failedJobs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CollectionJob job = redisTemplate.opsForList().index(FAILED_KEY, i);
            if (job != null) {
                failedJobs.add(job);
            }
        }
        
        return failedJobs;
    }
    
    /**
     * 특정 작업 재시도
     */
    public void retryJob(String jobId) {
        // Find job in failed queue
        List<CollectionJob> failedJobs = getFailedJobs();
        
        for (int i = 0; i < failedJobs.size(); i++) {
            CollectionJob job = failedJobs.get(i);
            if (jobId.equals(job.getJobId())) {
                // Remove from failed queue
                redisTemplate.opsForList().remove(FAILED_KEY, 1, job);
                
                // Reset retry count
                job.setRetryCount(0);
                job.setLastError(null);
                
                // Re-queue with immediate execution
                job.scheduleNow();
                redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, job, job.getScheduledAt());
                
                log.info("Retrying job: {}", jobId);
                return;
            }
        }
        
        log.warn("Job not found in failed queue: {}", jobId);
    }
    
    /**
     * 모든 실패한 작업 재시도
     */
    public void retryAllFailed() {
        List<CollectionJob> failedJobs = getFailedJobs();
        
        for (CollectionJob job : failedJobs) {
            // Remove from failed queue
            redisTemplate.opsForList().remove(FAILED_KEY, 1, job);
            
            // Reset retry count
            job.setRetryCount(0);
            job.setLastError(null);
            
            // Re-queue with immediate execution
            job.scheduleNow();
            redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, job, job.getScheduledAt());
        }
        
        log.info("Retrying {} failed jobs", failedJobs.size());
    }
}
