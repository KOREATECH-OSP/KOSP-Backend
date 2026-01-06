package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubCommitCollectionService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(name = "github.collection.worker.enabled", havingValue = "true", matchIfMissing = true)
public class GithubCollectionWorker {
    
    private static final String QUEUE_KEY = "github:collection:queue";
    private static final String PROCESSING_KEY = "github:collection:processing";
    private static final String FAILED_KEY = "github:collection:failed";
    private static final String COMPLETED_KEY = "github:collection:completed";
    
    private final RedisTemplate<String, CollectionJob> redisTemplate;
    private final GithubDataCollectionService dataCollectionService;
    private final GithubCommitCollectionService commitCollectionService;
    private final CollectionCompletionTracker completionTracker;
    private final TextEncryptor textEncryptor;
    
    public GithubCollectionWorker(
        @Qualifier("collectionJobRedisTemplate") RedisTemplate<String, CollectionJob> redisTemplate,
        GithubDataCollectionService dataCollectionService,
        GithubCommitCollectionService commitCollectionService,
        CollectionCompletionTracker completionTracker,
        TextEncryptor textEncryptor
    ) {
        this.redisTemplate = redisTemplate;
        this.dataCollectionService = dataCollectionService;
        this.commitCollectionService = commitCollectionService;
        this.completionTracker = completionTracker;
        this.textEncryptor = textEncryptor;
    }
    
    /**
     * 큐에서 작업을 가져와 처리
     */
    @Scheduled(fixedDelayString = "${github.collection.worker.poll-interval:1000}")
    public void processJobs() {
        try {
            CollectionJob job = pollJob();
            if (job == null) {
                return;
            }
            
            markAsProcessing(job);
            processJob(job);
            markAsCompleted(job);
            
        } catch (Exception e) {
            log.error("Error processing job", e);
        }
    }
    
    /**
     * 큐에서 작업 가져오기
     */
    private CollectionJob pollJob() {
        return redisTemplate.opsForList().leftPop(QUEUE_KEY);
    }
    
    /**
     * 작업 처리 중으로 표시
     */
    private void markAsProcessing(CollectionJob job) {
        job.setStartedAt(LocalDateTime.now());
        redisTemplate.opsForHash().put(PROCESSING_KEY, job.getJobId(), job);
    }
    
    /**
     * 작업 처리
     */
    private void processJob(CollectionJob job) {
        try {
            log.info("Processing job: {} (type: {})", job.getJobId(), job.getType());
            
            // Decrypt token from Redis (was re-encrypted before storing)
            String token = textEncryptor.decrypt(job.getEncryptedToken());
            
            log.debug("Decrypted token for job: {}", job.getJobId());
            
            // Debug: Check token format
            if (token != null) {
                log.debug("Token length: {}, starts with: {}", 
                    token.length(), 
                    token.length() > 10 ? token.substring(0, 10) + "..." : token);
            } else {
                log.error("Token is null for job: {}", job.getJobId());
            }
            
            switch (job.getType()) {
                case USER_BASIC -> dataCollectionService.collectUserBasicInfo(
                    job.getGithubLogin(),
                    token
                ).block();
                
                case USER_EVENTS -> dataCollectionService.collectUserEvents(
                    job.getGithubLogin(),
                    token
                ).block();
                
                case REPO_ISSUES -> dataCollectionService.collectIssues(
                    job.getRepoOwner(),
                    job.getRepoName(),
                    token
                ).block();
                
                case REPO_PRS -> dataCollectionService.collectPullRequests(
                    job.getRepoOwner(),
                    job.getRepoName(),
                    token
                ).block();
                
                case REPO_COMMITS -> commitCollectionService.collectAllCommits(
                    job.getRepoOwner(),
                    job.getRepoName(),
                    token
                ).block();
            }
            
            log.info("Successfully processed job: {}", job.getJobId());
            
        } catch (Exception e) {
            handleFailure(job, e);
            throw e;
        }
    }
    
    /**
     * 작업 완료 처리
     */
    private void markAsCompleted(CollectionJob job) {
        job.setCompletedAt(LocalDateTime.now());
        
        // Remove from processing
        redisTemplate.opsForHash().delete(PROCESSING_KEY, job.getJobId());
        
        // Add to completed (using hash instead of set)
        redisTemplate.opsForHash().put(COMPLETED_KEY, job.getJobId(), job);
        
        // Notify completion tracker
        if (job.getGithubLogin() != null) {
            completionTracker.decrementJobCount(job.getGithubLogin());
        }
    }
    
    /**
     * 실패 처리
     */
    private void handleFailure(CollectionJob job, Exception e) {
        job.setLastError(e.getMessage());
        job.setRetryCount(job.getRetryCount() + 1);
        
        // Remove from processing
        redisTemplate.opsForHash().delete(PROCESSING_KEY, job.getJobId());
        
        if (job.getRetryCount() < job.getMaxRetries()) {
            // Re-queue for retry
            log.warn("Job {} failed, retrying ({}/{}): {}", 
                job.getJobId(), job.getRetryCount(), job.getMaxRetries(), e.getMessage());
            redisTemplate.opsForList().rightPush(QUEUE_KEY, job);
        } else {
            // Move to failed queue
            log.error("Job {} failed permanently after {} retries: {}", 
                job.getJobId(), job.getRetryCount(), e.getMessage());
            redisTemplate.opsForList().rightPush(FAILED_KEY, job);
            
            // Notify completion tracker even for failed jobs
            // This ensures statistics calculation is triggered when all jobs complete
            if (job.getGithubLogin() != null) {
                completionTracker.decrementJobCount(job.getGithubLogin());
            }
        }
    }
}
