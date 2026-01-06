package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionCompletionTracker {
    
    private static final String QUEUE_KEY = "github:collection:queue";
    private static final String PROCESSING_KEY = "github:collection:processing";
    private static final String USER_JOBS_KEY = "github:collection:user:";
    
    private final RedisTemplate<String, CollectionJob> jobRedisTemplate;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    private final GithubStatisticsService statisticsService;
    
    // In-memory tracking of users being processed
    private final Set<String> processingUsers = ConcurrentHashMap.newKeySet();
    
    /**
     * 사용자의 작업 추적 시작
     */
    public void trackUserJobs(String githubLogin, int jobCount) {
        String key = USER_JOBS_KEY + githubLogin;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(jobCount));
        processingUsers.add(githubLogin);
        log.debug("Tracking {} jobs for user: {}", jobCount, githubLogin);
    }
    
    /**
     * 작업 완료 시 카운트 감소
     */
    public void decrementJobCount(String githubLogin) {
        String key = USER_JOBS_KEY + githubLogin;
        String currentCount = stringRedisTemplate.opsForValue().get(key);
        
        if (currentCount != null) {
            int remaining = Integer.parseInt(currentCount) - 1;
            
            if (remaining <= 0) {
                // 모든 작업 완료
                stringRedisTemplate.delete(key);
                processingUsers.remove(githubLogin);
                triggerStatisticsCalculation(githubLogin);
            } else {
                stringRedisTemplate.opsForValue().set(key, String.valueOf(remaining));
            }
        }
    }
    
    /**
     * 주기적으로 완료된 사용자 확인
     */
    @Scheduled(fixedDelay = 10000) // 10초마다
    public void checkCompletedUsers() {
        for (String githubLogin : processingUsers) {
            if (isUserCollectionComplete(githubLogin)) {
                triggerStatisticsCalculation(githubLogin);
                processingUsers.remove(githubLogin);
            }
        }
    }
    
    /**
     * 사용자의 모든 수집 작업이 완료되었는지 확인
     */
    private boolean isUserCollectionComplete(String githubLogin) {
        // Check if there are any pending or processing jobs for this user
        Long queueSize = jobRedisTemplate.opsForList().size(QUEUE_KEY);
        if (queueSize == null || queueSize == 0) {
            return true;
        }
        
        // Check processing jobs
        Set<Object> processingJobs = jobRedisTemplate.opsForHash().keys(PROCESSING_KEY);
        for (Object jobIdObj : processingJobs) {
            CollectionJob job = (CollectionJob) jobRedisTemplate.opsForHash().get(PROCESSING_KEY, jobIdObj);
            if (job != null && githubLogin.equals(job.getGithubLogin())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 통계 계산 트리거
     */
    private void triggerStatisticsCalculation(String githubLogin) {
        try {
            log.info("========================================");
            log.info("All collection jobs completed for user: {}", githubLogin);
            
            // Check for failed jobs
            Long failedCount = jobRedisTemplate.opsForList().size("github:collection:failed");
            if (failedCount != null && failedCount > 0) {
                log.warn("Found {} failed jobs in failed queue", failedCount);
                // Log failed jobs for this user
                List<CollectionJob> failedJobs = jobRedisTemplate.opsForList()
                    .range("github:collection:failed", 0, -1);
                if (failedJobs != null) {
                    failedJobs.stream()
                        .filter(job -> githubLogin.equals(job.getGithubLogin()))
                        .forEach(job -> log.warn("Failed job: {} - Type: {} - Error: {}", 
                            job.getJobId(), job.getType(), job.getLastError()));
                }
            }
            
            log.info("Triggering statistics calculation for user: {}", githubLogin);
            statisticsService.calculateAndSaveAllStatistics(githubLogin);
            log.info("Statistics calculation completed successfully for user: {}", githubLogin);
            log.info("========================================");
        } catch (Exception e) {
            log.error("Failed to calculate statistics for user: {}", githubLogin, e);
        }
    }
}
