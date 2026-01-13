package kr.ac.koreatech.sw.kosp.domain.github.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GitHub 데이터 수집 이벤트 통합 테스트")
@SuppressWarnings("removal") // @MockBean deprecation warning suppression
class GithubDataCollectionEventIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private GithubUserRepository githubUserRepository;

    @MockBean
    private GithubDataCollectionRetryService retryService;

    @Test
    @DisplayName("회원가입 이벤트 발행 시 데이터 수집 서비스 호출")
    void eventFlow_FromPublishToCollection() throws Exception {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token_123";
        
        GithubUser githubUser = GithubUser.builder()
            .githubId(12345L)
            .githubLogin(githubLogin)
            .githubToken(encryptedToken)
            .build();
        
        when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.of(githubUser));
        
        doNothing().when(retryService).collectWithRetry(githubLogin, encryptedToken);

        // when
        eventPublisher.publishEvent(new UserSignupEvent(this, githubLogin));

        // then - 데이터 수집이 호출되었는지 확인 (비동기이므로 timeout 사용)
        verify(retryService, timeout(5000)).collectWithRetry(githubLogin, encryptedToken);
    }

    @Test
    @DisplayName("GitHub 사용자가 없으면 데이터 수집하지 않음")
    void eventFlow_UserNotFound_NoDataCollection() throws Exception {
        // given
        String githubLogin = "nonexistent";
        
        when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.empty());

        // when
        eventPublisher.publishEvent(new UserSignupEvent(this, githubLogin));

        // then - 잠시 대기 후 호출되지 않았는지 확인
        Thread.sleep(2000);
        verify(retryService, org.mockito.Mockito.never()).collectWithRetry(any(), any());
    }
}
