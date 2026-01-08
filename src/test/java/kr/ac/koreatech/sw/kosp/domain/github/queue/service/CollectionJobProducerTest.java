package kr.ac.koreatech.sw.kosp.domain.github.queue.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJob;
import kr.ac.koreatech.sw.kosp.domain.github.queue.model.CollectionJobType;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollectionJobProducer 단위 테스트")
class CollectionJobProducerTest {
    
    @Mock
    private RedisTemplate<String, CollectionJob> redisTemplate;
    
    @Mock
    private CollectionCompletionTracker completionTracker;
    
    @Mock
    private ZSetOperations<String, CollectionJob> zSetOperations;
    
    @InjectMocks
    private CollectionJobProducer producer;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }
    
    @Test
    @DisplayName("사용자 수집 작업을 큐에 추가한다")
    void enqueueUserCollection() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueUserCollection(githubLogin, encryptedToken);
        
        // then
        verify(zSetOperations, times(2)).add(eq("github:collection:priority_queue"), any(CollectionJob.class), anyDouble());
        verify(completionTracker).trackUserJobs(githubLogin, 2);
    }
    
    @Test
    @DisplayName("레포지토리 수집 작업을 큐에 추가한다")
    void enqueueRepositoryCollection() {
        // given
        String repoOwner = "owner";
        String repoName = "repo";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueRepositoryCollection(repoOwner, repoName, encryptedToken);
        
        // then
        verify(zSetOperations, times(3)).add(eq("github:collection:priority_queue"), any(CollectionJob.class), anyDouble());
    }
    
    @Test
    @DisplayName("작업에 고유 ID가 할당된다")
    void jobIdIsUnique() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueUserCollection(githubLogin, encryptedToken);
        
        // then
        verify(zSetOperations, times(2)).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getJobId() != null && !job.getJobId().isEmpty()
        ), anyDouble());
    }
    
    @Test
    @DisplayName("작업에 스케줄 타임스탬프가 설정된다")
    void jobHasScheduledTimestamp() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        long before = System.currentTimeMillis();
        
        // when
        producer.enqueueUserCollection(githubLogin, encryptedToken);
        
        long after = System.currentTimeMillis();
        
        // then
        verify(zSetOperations, times(2)).add(eq("github:collection:priority_queue"), argThat(job -> {
            long scheduledAt = job.getScheduledAt();
            return scheduledAt >= before && scheduledAt <= after;
        }), anyDouble());
    }
    
    @Test
    @DisplayName("사용자 작업은 우선순위가 설정된다")
    void userJobsHavePriority() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueUserCollection(githubLogin, encryptedToken);
        
        // then
        verify(zSetOperations).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getType() == CollectionJobType.USER_BASIC && job.getPriority() == 1
        ), anyDouble());
        verify(zSetOperations).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getType() == CollectionJobType.USER_EVENTS && job.getPriority() == 2
        ), anyDouble());
    }
    
    @Test
    @DisplayName("레포지토리 작업은 우선순위가 설정된다")
    void repositoryJobsHavePriority() {
        // given
        String repoOwner = "owner";
        String repoName = "repo";
        String encryptedToken = "encrypted_token";
        
        // when
        producer.enqueueRepositoryCollection(repoOwner, repoName, encryptedToken);
        
        // then
        verify(zSetOperations).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getType() == CollectionJobType.REPO_ISSUES && job.getPriority() == 3
        ), anyDouble());
        verify(zSetOperations).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getType() == CollectionJobType.REPO_PRS && job.getPriority() == 3
        ), anyDouble());
        verify(zSetOperations).add(eq("github:collection:priority_queue"), argThat(job -> 
            job.getType() == CollectionJobType.REPO_COMMITS && job.getPriority() == 4
        ), anyDouble());
    }
}
