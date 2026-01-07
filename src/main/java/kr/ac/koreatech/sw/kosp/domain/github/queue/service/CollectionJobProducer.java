package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJobType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CollectionJobProducer {
    
    private static final String PRIORITY_QUEUE_KEY = "github:collection:priority_queue";
    private static final int DEFAULT_MAX_RETRIES = 5;  // 3 → 5로 증가
    
    private final RedisTemplate<String, CollectionJob> redisTemplate;
    private final CollectionCompletionTracker completionTracker;
    
    public CollectionJobProducer(
        @Qualifier("collectionJobRedisTemplate") RedisTemplate<String, CollectionJob> redisTemplate,
        CollectionCompletionTracker completionTracker
    ) {
        this.redisTemplate = redisTemplate;
        this.completionTracker = completionTracker;
    }
    
    /**
     * 사용자 전체 데이터 수집 작업을 큐에 추가
     */
    public void enqueueUserCollection(String githubLogin, String encryptedToken) {
        // 1. User basic info (highest priority)
        enqueue(CollectionJob.builder()
            .type(CollectionJobType.USER_BASIC)
            .githubLogin(githubLogin)
            .encryptedToken(encryptedToken)
            .priority(1)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build());
        
        // 2. User events
        enqueue(CollectionJob.builder()
            .type(CollectionJobType.USER_EVENTS)
            .githubLogin(githubLogin)
            .encryptedToken(encryptedToken)
            .priority(2)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build());
        
        // Track 2 jobs for this user (basic + events)
        // Repository jobs will be tracked separately
        completionTracker.trackUserJobs(githubLogin, 2);
        
        log.info("Enqueued user collection jobs for: {}", githubLogin);
    }
    
    /**
     * 레포지토리 데이터 수집 작업을 큐에 추가
     */
    public void enqueueRepositoryCollection(String repoOwner, String repoName, String encryptedToken) {
        // Issues
        enqueue(CollectionJob.builder()
            .type(CollectionJobType.REPO_ISSUES)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .encryptedToken(encryptedToken)
            .priority(3)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build());
        
        // PRs
        enqueue(CollectionJob.builder()
            .type(CollectionJobType.REPO_PRS)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .encryptedToken(encryptedToken)
            .priority(3)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build());
        
        // Commits
        enqueue(CollectionJob.builder()
            .type(CollectionJobType.REPO_COMMITS)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .encryptedToken(encryptedToken)
            .priority(4)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .build());
        
        log.info("Enqueued repository collection jobs for: {}/{}", repoOwner, repoName);
    }
    
    /**
     * 즉시 실행 작업 추가
     */
    public void enqueue(CollectionJob job) {
        enqueueWithDelay(job, 0);
    }
    
    /**
     * 지연 실행 작업 추가
     */
    public void enqueueWithDelay(CollectionJob job, long delayMillis) {
        job.setJobId(UUID.randomUUID().toString());
        job.setCreatedAt(LocalDateTime.now());
        job.setRetryCount(0);
        job.scheduleAfter(delayMillis);
        
        // Redis Sorted Set에 추가 (Score = scheduledAt timestamp)
        redisTemplate.opsForZSet().add(
            PRIORITY_QUEUE_KEY,
            job,
            job.getScheduledAt()
        );
        
        if (delayMillis > 0) {
            log.debug("Enqueued job {} to execute in {} ms (type: {}, priority: {})",
                job.getJobId(), delayMillis, job.getType(), job.getPriority());
        } else {
            log.debug("Enqueued job: {} (type: {}, priority: {})",
                job.getJobId(), job.getType(), job.getPriority());
        }
    }
    
    /**
     * Rate limit 리셋 후 실행 작업 추가
     */
    public void enqueueAfterRateLimitReset(CollectionJob job, long resetTime) {
        long delayMillis = resetTime - System.currentTimeMillis();
        
        if (delayMillis < 0) {
            delayMillis = 0;  // 이미 리셋됨
        }
        
        job.scheduleAt(resetTime);
        
        redisTemplate.opsForZSet().add(
            PRIORITY_QUEUE_KEY,
            job,
            resetTime
        );
        
        log.info("Job {} scheduled after rate limit reset in {} minutes",
            job.getJobId(),
            delayMillis / 60000);
    }
}
