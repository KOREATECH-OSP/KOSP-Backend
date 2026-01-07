package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.queue.dto.QueueStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis Queue 통합 테스트")
class RedisQueueIntegrationTest {
    
    @Autowired
    private CollectionJobProducer producer;
    
    @Autowired
    private CollectionJobMonitoringService monitoringService;
    
    @Autowired
    private RedisTemplate<String, CollectionJob> redisTemplate;
    
    private static final String PRIORITY_QUEUE_KEY = "github:collection:priority_queue";
    private static final String PROCESSING_KEY = "github:collection:processing";
    private static final String FAILED_KEY = "github:collection:failed";
    private static final String COMPLETED_KEY = "github:collection:completed";
    
    @BeforeEach
    void setUp() {
        // Clear all queues before each test
        redisTemplate.delete(PRIORITY_QUEUE_KEY);
        redisTemplate.delete(PROCESSING_KEY);
        redisTemplate.delete(FAILED_KEY);
        redisTemplate.delete(COMPLETED_KEY);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after tests
        redisTemplate.delete(PRIORITY_QUEUE_KEY);
        redisTemplate.delete(PROCESSING_KEY);
        redisTemplate.delete(FAILED_KEY);
        redisTemplate.delete(COMPLETED_KEY);
    }
    
    @Test
    @DisplayName("사용자 수집 작업을 큐에 추가하고 통계를 확인할 수 있다")
    void enqueueUserCollectionAndCheckStats() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueUserCollection(githubLogin, encryptedToken);
        
        // then
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                QueueStatsResponse stats = monitoringService.getQueueStats();
                assertThat(stats.getQueueLength()).isEqualTo(2); // USER_BASIC + USER_EVENTS
            });
    }
    
    @Test
    @DisplayName("레포지토리 수집 작업을 큐에 추가하고 통계를 확인할 수 있다")
    void enqueueRepositoryCollectionAndCheckStats() {
        // given
        String repoOwner = "owner";
        String repoName = "repo";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueRepositoryCollection(repoOwner, repoName, encryptedToken);
        
        // then
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                QueueStatsResponse stats = monitoringService.getQueueStats();
                assertThat(stats.getQueueLength()).isEqualTo(3); // ISSUES + PRS + COMMITS
            });
    }
    
    @Test
    @DisplayName("여러 작업을 큐에 추가할 수 있다")
    void enqueueMultipleJobs() {
        // given
        String githubLogin1 = "user1";
        String githubLogin2 = "user2";
        String encryptedToken = "token";
        
        // when
        producer.enqueueUserCollection(githubLogin1, encryptedToken);
        producer.enqueueUserCollection(githubLogin2, encryptedToken);
        
        // then
        await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                QueueStatsResponse stats = monitoringService.getQueueStats();
                assertThat(stats.getQueueLength()).isEqualTo(4); // 2 users × 2 jobs each
            });
    }
    
    @Test
    @DisplayName("실패한 작업을 조회할 수 있다")
    void getFailedJobs() {
        // given - No failed jobs initially
        
        // when
        List<CollectionJob> failedJobs = monitoringService.getFailedJobs();
        
        // then
        assertThat(failedJobs).isEmpty();
    }
    
    @Test
    @DisplayName("큐 통계가 정확하다")
    void queueStatsAreAccurate() {
        // given - Empty queues
        
        // when
        QueueStatsResponse stats = monitoringService.getQueueStats();
        
        // then
        assertThat(stats.getQueueLength()).isEqualTo(0);
        assertThat(stats.getProcessingCount()).isEqualTo(0);
        assertThat(stats.getFailedCount()).isEqualTo(0);
        assertThat(stats.getCompletedCount()).isEqualTo(0);
    }
}
