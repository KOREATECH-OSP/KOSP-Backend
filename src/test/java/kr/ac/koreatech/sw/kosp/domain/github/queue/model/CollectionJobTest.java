package kr.ac.koreatech.sw.kosp.domain.github.queue.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CollectionJob 단위 테스트")
class CollectionJobTest {
    
    @Test
    @DisplayName("Builder로 작업을 생성할 수 있다")
    void buildJob() {
        // given
        String jobId = "job-123";
        CollectionJobType type = CollectionJobType.USER_BASIC;
        String githubLogin = "testuser";
        String encryptedToken = "token";
        
        // when
        CollectionJob job = CollectionJob.builder()
            .jobId(jobId)
            .type(type)
            .githubLogin(githubLogin)
            .encryptedToken(encryptedToken)
            .priority(1)
            .maxRetries(3)
            .build();
        
        // then
        assertThat(job.getJobId()).isEqualTo(jobId);
        assertThat(job.getType()).isEqualTo(type);
        assertThat(job.getGithubLogin()).isEqualTo(githubLogin);
        assertThat(job.getEncryptedToken()).isEqualTo(encryptedToken);
        assertThat(job.getPriority()).isEqualTo(1);
        assertThat(job.getMaxRetries()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("레포지토리 작업을 생성할 수 있다")
    void buildRepositoryJob() {
        // given
        String repoOwner = "owner";
        String repoName = "repo";
        
        // when
        CollectionJob job = CollectionJob.builder()
            .jobId("job-456")
            .type(CollectionJobType.REPO_ISSUES)
            .repoOwner(repoOwner)
            .repoName(repoName)
            .encryptedToken("token")
            .priority(3)
            .maxRetries(3)
            .build();
        
        // then
        assertThat(job.getRepoOwner()).isEqualTo(repoOwner);
        assertThat(job.getRepoName()).isEqualTo(repoName);
    }
    
    @Test
    @DisplayName("작업 상태를 업데이트할 수 있다")
    void updateJobState() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-789")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .priority(1)
            .maxRetries(3)
            .build();
        
        // when
        LocalDateTime startedAt = LocalDateTime.now();
        job.setStartedAt(startedAt);
        job.setRetryCount(1);
        job.setLastError("Test error");
        
        // then
        assertThat(job.getStartedAt()).isEqualTo(startedAt);
        assertThat(job.getRetryCount()).isEqualTo(1);
        assertThat(job.getLastError()).isEqualTo("Test error");
    }
    
    @Test
    @DisplayName("작업 완료 시간을 설정할 수 있다")
    void setCompletedAt() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-complete")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .priority(1)
            .maxRetries(3)
            .build();
        
        // when
        LocalDateTime completedAt = LocalDateTime.now();
        job.setCompletedAt(completedAt);
        
        // then
        assertThat(job.getCompletedAt()).isEqualTo(completedAt);
    }
    
    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있다")
    void incrementRetryCount() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-retry")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .priority(1)
            .maxRetries(3)
            .retryCount(0)
            .build();
        
        // when
        job.incrementRetryCount();
        job.incrementRetryCount();
        
        // then
        assertThat(job.getRetryCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("지연 실행 스케줄링을 설정할 수 있다")
    void scheduleAfter() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-delay")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .build();
        
        long before = System.currentTimeMillis();
        
        // when
        job.scheduleAfter(5000);  // 5초 후
        
        long after = System.currentTimeMillis();
        
        // then
        assertThat(job.getScheduledAt()).isGreaterThanOrEqualTo(before + 5000);
        assertThat(job.getScheduledAt()).isLessThanOrEqualTo(after + 5000);
    }
    
    @Test
    @DisplayName("즉시 실행 스케줄링을 설정할 수 있다")
    void scheduleNow() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-now")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .build();
        
        long before = System.currentTimeMillis();
        
        // when
        job.scheduleNow();
        
        long after = System.currentTimeMillis();
        
        // then
        assertThat(job.getScheduledAt()).isGreaterThanOrEqualTo(before);
        assertThat(job.getScheduledAt()).isLessThanOrEqualTo(after);
    }
    
    @Test
    @DisplayName("실행 가능 여부를 확인할 수 있다")
    void isReadyToExecute() {
        // given
        CollectionJob job = CollectionJob.builder()
            .jobId("job-ready")
            .type(CollectionJobType.USER_BASIC)
            .githubLogin("testuser")
            .encryptedToken("token")
            .build();
        
        // when - 과거 시간으로 스케줄
        job.scheduleAt(System.currentTimeMillis() - 1000);
        
        // then
        assertThat(job.isReadyToExecute()).isTrue();
        
        // when - 미래 시간으로 스케줄
        job.scheduleAt(System.currentTimeMillis() + 10000);
        
        // then
        assertThat(job.isReadyToExecute()).isFalse();
    }
}
