package kr.ac.koreatech.sw.kosp.domain.github.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionRetryService;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubDataCollectionEventListener {
    
    private final GithubDataCollectionRetryService retryService;
    private final GithubUserRepository githubUserRepository;
    
    @EventListener
    public void handleUserSignup(UserSignupEvent event) {
        String githubLogin = event.getGithubLogin();
        
        log.info("Queuing initial data collection for new user: {}", githubLogin);
        
        try {
            // GithubUser에서 암호화된 토큰 조회
            GithubUser githubUser = githubUserRepository.findByGithubLogin(githubLogin)
                .orElseThrow(() -> new EntityNotFoundException("GitHub user not found: " + githubLogin));
            
            // 비동기로 재시도 가능한 수집 시작
            retryService.collectWithRetry(githubLogin, githubUser.getGithubToken());
            
        } catch (Exception e) {
            log.error("Failed to queue data collection for user: {}", githubLogin, e);
        }
    }
}
