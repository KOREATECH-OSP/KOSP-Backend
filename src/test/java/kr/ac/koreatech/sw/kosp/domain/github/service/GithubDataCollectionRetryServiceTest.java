package kr.ac.koreatech.sw.kosp.domain.github.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import kr.ac.koreatech.sw.kosp.domain.github.exception.RateLimitExceededException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubDataCollectionRetryService 테스트")
class GithubDataCollectionRetryServiceTest {

    @Mock
    private GithubStatisticsService statisticsService;

    @Mock
    private TextEncryptor textEncryptor;

    @InjectMocks
    private GithubDataCollectionRetryService retryService;

    @Test
    @DisplayName("정상적으로 데이터 수집 및 통계 계산")
    void collectWithRetry_Success() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        String decryptedToken = "decrypted_token";
        
        when(textEncryptor.decrypt(encryptedToken)).thenReturn(decryptedToken);

        // when
        retryService.collectWithRetry(githubLogin, encryptedToken);

        // then
        verify(textEncryptor).decrypt(encryptedToken);
        verify(statisticsService).calculateAndSaveAllStatistics(githubLogin);
    }

    @Test
    @DisplayName("Rate Limit 초과 시 재시도")
    void collectWithRetry_RateLimitExceeded_Retry() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        String decryptedToken = "decrypted_token";
        
        when(textEncryptor.decrypt(encryptedToken)).thenReturn(decryptedToken);
        
        // 첫 번째 시도 실패, 두 번째 성공
        org.mockito.Mockito.doThrow(new RateLimitExceededException("Rate limit exceeded", 
                System.currentTimeMillis() + 100))
            .doNothing()
            .when(statisticsService).calculateAndSaveAllStatistics(githubLogin);

        // when
        retryService.collectWithRetry(githubLogin, encryptedToken);

        // then
        verify(statisticsService, times(2)).calculateAndSaveAllStatistics(githubLogin);
    }

    @Test
    @DisplayName("일반 예외 발생 시 재시도하지 않음")
    void collectWithRetry_GeneralException_NoRetry() {
        // given
        String githubLogin = "testuser";
        String encryptedToken = "encrypted_token";
        String decryptedToken = "decrypted_token";
        
        when(textEncryptor.decrypt(encryptedToken)).thenReturn(decryptedToken);
        org.mockito.Mockito.doThrow(new RuntimeException("Unexpected error"))
            .when(statisticsService).calculateAndSaveAllStatistics(githubLogin);

        // when
        retryService.collectWithRetry(githubLogin, encryptedToken);

        // then
        verify(statisticsService, times(1)).calculateAndSaveAllStatistics(githubLogin);
    }
}
