package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubCommitCollectionService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "github.collection.worker.enabled", havingValue = "true", matchIfMissing = true)
public class GithubCollectionWorker {
    
    private static final String PRIORITY_QUEUE_KEY = "github:collection:priority_queue";
    private static final String PROCESSING_KEY = "github:collection:processing";
    private static final String FAILED_KEY = "github:collection:failed";
    private static final String COMPLETED_KEY = "github:collection:completed";
    private static final int MAX_RETRY = 5;
    
    private final RedisTemplate<String, CollectionJob> redisTemplate;
    private final GithubDataCollectionService dataCollectionService;
    private final GithubCommitCollectionService commitCollectionService;
    private final CollectionCompletionTracker completionTracker;
    private final TextEncryptor textEncryptor;
    private final kr.ac.koreatech.sw.kosp.domain.github.service.GithubRateLimitChecker rateLimitChecker;
    private final CollectionJobProducer jobProducer;
    
    public GithubCollectionWorker(
        @Qualifier("collectionJobRedisTemplate") RedisTemplate<String, CollectionJob> redisTemplate,
        GithubDataCollectionService dataCollectionService,
        GithubCommitCollectionService commitCollectionService,
        CollectionCompletionTracker completionTracker,
        TextEncryptor textEncryptor,
        kr.ac.koreatech.sw.kosp.domain.github.service.GithubRateLimitChecker rateLimitChecker,
        CollectionJobProducer jobProducer
    ) {
        this.redisTemplate = redisTemplate;
        this.dataCollectionService = dataCollectionService;
        this.commitCollectionService = commitCollectionService;
        this.completionTracker = completionTracker;
        this.textEncryptor = textEncryptor;
        this.rateLimitChecker = rateLimitChecker;
        this.jobProducer = jobProducer;
    }
    
    /**
     * 큐에서 작업을 가져와 처리
     * @Async로 여러 Worker가 병렬로 실행됨 (CPU 코어 수만큼)
     */
    @Async("githubWorkerExecutor")
    @Scheduled(fixedDelayString = "${github.collection.worker.poll-interval:1000}")
    public void processJobs() {
        CollectionJob job = null;
        try {
            job = pollJob();
            if (job == null) {
                return;
            }
            
            markAsProcessing(job);
            processJob(job);
            markAsCompleted(job);
            
        } catch (kr.ac.koreatech.sw.kosp.domain.github.client.rest.RateLimitException e) {
            // ✅ Rate Limit 도달 - 작업을 재스케줄 (스레드 블로킹 없음!)
            if (job != null) {
                long waitMillis = e.getWaitTime().toMillis();
                log.warn("⚠️ Rate limit reached for job {}. Rescheduling after {} ms", 
                    job.getJobId(), waitMillis);
                
                // Remove from processing and reschedule
                redisTemplate.opsForHash().delete(PROCESSING_KEY, job.getJobId());
                jobProducer.enqueueWithDelay(job, waitMillis);
            }
            // ❌ 예외를 다시 던지지 않음 - Worker는 즉시 다음 작업 처리
            
        } catch (Exception e) {
            // ✅ 명확한 에러 로깅 추가
            if (job != null) {
                log.error("❌ Worker failed to process job {}: {}", 
                    job.getJobId(), e.getMessage(), e);
            } else {
                log.error("❌ Worker error: {}", e.getMessage(), e);
            }
            // Exception은 이미 processJob()의 handleFailure()에서 처리됨
            // 여기서는 로깅만 하고 계속 진행
        }
    }

    /**
     * 우선순위 큐에서 실행 가능한 작업 가져오기
     */
    private CollectionJob pollJob() {
        long now = System.currentTimeMillis();
        
        // 현재 시간보다 작은 score를 가진 작업만 조회 (1개)
        var readyJobs = redisTemplate.opsForZSet()
            .rangeByScore(PRIORITY_QUEUE_KEY, 0, now, 0, 1);
        
        if (readyJobs == null || readyJobs.isEmpty()) {
            return null;
        }
        
        CollectionJob job = readyJobs.iterator().next();
        
        // Atomic remove (다른 Worker와 경합 방지)
        Long removed = redisTemplate.opsForZSet().remove(PRIORITY_QUEUE_KEY, job);
        if (removed == null || removed == 0) {
            return null;  // 다른 Worker가 이미 처리
        }
        
        return job;
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
            
            // Token from Redis is 1x encrypted (from DB's 2x → 1x after @Convert)
            // Decrypt it to get plain text for GitHub API calls
            String encryptedToken = job.getEncryptedToken();
            
            if (encryptedToken == null || encryptedToken.isEmpty()) {
                log.error("Encrypted token is null or empty for job: {}", job.getJobId());
                throw new IllegalStateException("GitHub token is required");
            }
            
            String token = textEncryptor.decrypt(encryptedToken);
            
            log.debug("Decrypted token for job: {} (length: {})", 
                job.getJobId(), 
                token != null ? token.length() : 0);
            
            if (token == null || token.isEmpty()) {
                log.error("Decrypted token is null or empty for job: {}", job.getJobId());
                throw new IllegalStateException("Failed to decrypt GitHub token");
            }
            
            // ✅ Rate limit 체크 제거 - GithubRestApiClient가 응답 헤더로 자동 관리
            
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
                
                
                case REPO_COMMITS -> {
                    // githubLogin 검증
                    if (job.getGithubLogin() == null || job.getGithubLogin().isEmpty()) {
                        log.error("❌ REPO_COMMITS job missing githubLogin: {}", job.getJobId());
                        throw new IllegalArgumentException(
                            "githubLogin is required for REPO_COMMITS collection"
                        );
                    }
                    
                    Long count = commitCollectionService.collectAllCommits(
                        job.getRepoOwner(),
                        job.getRepoName(),
                        job.getGithubLogin(),  // ✅ 추가
                        token
                    ).block();
                    
                    log.info("✅ Collected {} commits for {} in {}/{}", 
                        count, job.getGithubLogin(), job.getRepoOwner(), job.getRepoName());
                    
                    return;  // Fixed: changed from yield to return
                }
            }
            
            log.info("Successfully processed job: {}", job.getJobId());
            
        } catch (kr.ac.koreatech.sw.kosp.domain.github.client.rest.RateLimitException e) {
            // ✅ RateLimitException은 실패로 처리하지 않고 그대로 전파
            // processJobs()에서 잡아서 긴 대기 시간으로 재스케줄링함
            throw e;
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
        job.incrementRetryCount();
        
        // ✅ Remove from processing FIRST (중요!)
        redisTemplate.opsForHash().delete(PROCESSING_KEY, job.getJobId());
        
        if (job.getRetryCount() < MAX_RETRY) {
            // Exponential backoff으로 재스케줄
            long delayMillis = (long) Math.pow(2, job.getRetryCount()) * 1000;  // 2^n seconds
            
            log.warn("⚠️ Job {} failed, retrying in {} seconds ({}/{}): {}",
                job.getJobId(), delayMillis / 1000, job.getRetryCount(), MAX_RETRY, e.getMessage());
            
            jobProducer.enqueueWithDelay(job, delayMillis);
        } else {
            // Move to failed queue
            log.error("❌ Job {} permanently failed after {} retries: {}",
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
