package kr.ac.koreatech.sw.kosp.domain.github.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionRetryService;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubDataCollectionEventListener 테스트")
class GithubDataCollectionEventListenerTest {

    @Mock
    private GithubDataCollectionRetryService retryService;

    @Mock
    private GithubUserRepository githubUserRepository;

    @InjectMocks
    private GithubDataCollectionEventListener listener;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 데이터 수집 시작")
    void handleUserSignup_Success() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token_123";
        
        GithubUser githubUser = GithubUser.builder()
            .githubId(12345L)
            .githubLogin(githubLogin)
            .githubToken(encryptedToken)
            .build();
        
        UserSignupEvent event = new UserSignupEvent(this, githubLogin);
        
        when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.of(githubUser));

        // when
        listener.handleUserSignup(event);

        // then
        verify(githubUserRepository).findByGithubLogin(githubLogin);
        verify(retryService).collectWithRetry(githubLogin, encryptedToken);
    }

    @Test
    @DisplayName("GitHub 사용자가 없으면 수집하지 않음")
    void handleUserSignup_UserNotFound() {
        // given
        String githubLogin = "nonexistent";
        UserSignupEvent event = new UserSignupEvent(this, githubLogin);
        
        when(githubUserRepository.findByGithubLogin(githubLogin))
            .thenReturn(Optional.empty());

        // when
        listener.handleUserSignup(event);

        // then
        verify(githubUserRepository).findByGithubLogin(githubLogin);
        verify(retryService, never()).collectWithRetry(any(), any());
    }
}
