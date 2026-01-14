package kr.ac.koreatech.sw.kosp.domain.github.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/test/github")
@RequiredArgsConstructor
@Profile("!prod")  // Production 환경에서는 비활성화
@Tag(name = "🧪 GitHub Test", description = "테스트용 GitHub 데이터 수집 API (개발 환경 전용)")
public class GithubTestController {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final kr.ac.koreatech.sw.kosp.domain.github.service.GithubRateLimitChecker rateLimitChecker;
    private final org.springframework.security.crypto.encrypt.TextEncryptor textEncryptor;

    /**
     * 테스트용: GitHub 로그인으로 데이터 수집 이벤트 발생
     */
    @Operation(
        summary = "GitHub 로그인으로 데이터 수집 트리거",
        description = "GitHub 로그인 이름으로 사용자를 찾아 데이터 수집을 시작합니다."
    )
    @PostMapping("/collect/github/{githubLogin}")
    public ResponseEntity<String> triggerCollectionByGithubLogin(@PathVariable String githubLogin) {
        User user = userRepository.findByGithubUser_GithubLogin(githubLogin)
            .orElseThrow(() -> new IllegalArgumentException("User not found with GitHub login: " + githubLogin));

        log.info("🧪 [TEST] Triggering GitHub data collection for GitHub user: {}", githubLogin);
        
        // 직접 큐에 작업 추가
        eventPublisher.publishEvent(new UserSignupEvent(this, user.getGithubUser().getGithubLogin()));
        
        return ResponseEntity.ok(String.format(
            "✅ Collection triggered for GitHub user: %s\n" +
            "- USER_BASIC job added\n" +
            "- USER_EVENTS job added\n" +
            "Check logs for progress.", 
            githubLogin
        ));
    }

    /**
     * 테스트용: GitHub 통계 수동 계산 (강제 트리거)
     */
    @Operation(summary = "GitHub 통계 수동 계산 (강제 트리거)", description = "수집 완료 여부와 관계없이 즉시 통계 계산 이벤트를 발행합니다.")
    @PostMapping("/calculate/statistics/{githubLogin}")
    public ResponseEntity<String> calculateStatistics(@PathVariable String githubLogin) {
        log.info("🧪 [TEST] Publishing statistics calculation event for: {}", githubLogin);
        eventPublisher.publishEvent(new kr.ac.koreatech.sw.kosp.domain.github.event.UserStatisticsCalculationRequestedEvent(this, githubLogin));
        return ResponseEntity.ok("✅ Statistics calculation event published for: " + githubLogin);
    }
    
    /**
     * 테스트용: GitHub API Rate Limit 확인
     */
    @Operation(
        summary = "GitHub API Rate Limit 확인",
        description = "특정 사용자의 GitHub 토큰으로 현재 API Rate Limit 상태를 확인합니다."
    )
    @org.springframework.web.bind.annotation.GetMapping("/rate-limit/{githubLogin}")
    public ResponseEntity<String> checkRateLimit(@PathVariable String githubLogin) {
        User user = userRepository.findByGithubUser_GithubLogin(githubLogin)
            .orElseThrow(() -> new IllegalArgumentException("User not found with GitHub login: " + githubLogin));
        
        String encryptedToken = user.getGithubUser().getGithubToken();
        
        log.info("🧪 [TEST] Checking rate limit for GitHub user: {}", githubLogin);
        
        // Decrypt token before API call
        String plainToken = textEncryptor.decrypt(encryptedToken);
        
        var rateLimitInfo = rateLimitChecker.checkRateLimit(plainToken);
        
        long resetTimeSeconds = (rateLimitInfo.resetTime() - System.currentTimeMillis()) / 1000;
        long resetMinutes = resetTimeSeconds / 60;
        
        return ResponseEntity.ok(String.format(
            "📊 GitHub API Rate Limit Status\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "User: %s\n" +
            "Limit: %d requests/hour\n" +
            "Remaining: %d requests\n" +
            "Used: %d requests\n" +
            "Reset in: %d minutes (%d seconds)\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            githubLogin,
            rateLimitInfo.limit(),
            rateLimitInfo.remaining(),
            rateLimitInfo.limit() - rateLimitInfo.remaining(),
            resetMinutes,
            resetTimeSeconds
        ));
    }
}
