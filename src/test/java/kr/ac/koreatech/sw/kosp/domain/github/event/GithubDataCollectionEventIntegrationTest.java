package kr.ac.koreatech.sw.kosp.domain.github.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionRetryService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GitHub 데이터 수집 이벤트 통합 테스트")
class GithubDataCollectionEventIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private GithubDataCollectionRetryService retryService;

    @MockBean
    private GithubUserRepository githubUserRepository;

    @MockBean
    private GithubStatisticsService statisticsService;

    @Test
    @DisplayName("회원가입 이벤트 발행 시 전체 흐름 검증")
    void eventFlow_FromPublishToCollection() throws Exception {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token_123";
        
        GithubUser githubUser = GithubUser.builder()
            .githubId(12345L)
            .githubLogin(githubLogin)
            .githubToken(encryptedToken)
            .build();
        
        // Mock repository
        org.mockito.Mockito.when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.of(githubUser));

        // when
        eventPublisher.publishEvent(new UserSignupEvent(this, githubLogin));

        // then - 통계 계산이 호출되었는지 확인 (비동기이므로 timeout 사용)
        verify(statisticsService, timeout(5000)).calculateAndSaveAllStatistics(githubLogin);
    }

    @Test
    @DisplayName("GitHub 사용자가 없으면 통계 계산하지 않음")
    void eventFlow_UserNotFound_NoStatisticsCalculation() throws Exception {
        // given
        String githubLogin = "nonexistent";
        
        org.mockito.Mockito.when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.empty());

        // when
        eventPublisher.publishEvent(new UserSignupEvent(this, githubLogin));

        // then - 잠시 대기 후 호출되지 않았는지 확인
        Thread.sleep(2000);
        verify(statisticsService, org.mockito.Mockito.never()).calculateAndSaveAllStatistics(any());
    }
}
