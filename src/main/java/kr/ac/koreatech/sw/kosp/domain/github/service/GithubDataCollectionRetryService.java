package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.exception.RateLimitExceededException;
import kr.ac.koreatech.sw.kosp.domain.github.model.RateLimitInfo;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubDataCollectionRetryService {

    private static final int MAX_RETRIES = 3;
    private static final int RATE_LIMIT_THRESHOLD = 100;
    private static final long EXTRA_WAIT_MILLIS = 1000;
    private static final long MIN_WAIT_MILLIS = 60000;

    private final GithubDataCollectionService dataCollectionService;
    private final GithubStatisticsService statisticsService;
    private final GithubRateLimitChecker rateLimitChecker;
    private final GithubRestApiClient restApiClient;
    private final TextEncryptor textEncryptor;

    @Async
    public void collectWithRetry(String githubLogin, String encryptedToken) {
        String token = textEncryptor.decrypt(encryptedToken);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            boolean success = tryCollectData(githubLogin, token, attempt);

            if (success) {
                return;
            }
        }
    }

    private boolean tryCollectData(String githubLogin, String token, int attempt) {
        try {
            logAttempt(githubLogin, attempt);
            waitIfRateLimitLow(githubLogin, token);
            collectAndCalculate(githubLogin, token);
            logSuccess(githubLogin);
            return true;

        } catch (RateLimitExceededException exception) {
            return handleRateLimitException(githubLogin, exception, attempt);
        } catch (Exception exception) {
            logUnexpectedError(githubLogin, exception);
            return false;
        }
    }

    private void waitIfRateLimitLow(String githubLogin, String token) throws InterruptedException {
        RateLimitInfo rateLimitInfo = rateLimitChecker.checkRateLimit(token);

        if (rateLimitInfo.remaining() >= RATE_LIMIT_THRESHOLD) {
            return;
        }

        long waitTime = rateLimitInfo.getWaitTimeMillis();
        logRateLimitWait(githubLogin, rateLimitInfo.remaining(), waitTime);
        Thread.sleep(waitTime + EXTRA_WAIT_MILLIS);
    }

    private void collectAndCalculate(String githubLogin, String token) {
        collectAllData(githubLogin, token);
        statisticsService.calculateAndSaveAllStatistics(githubLogin);
    }

    private boolean handleRateLimitException(String githubLogin, RateLimitExceededException exception, int attempt) {
        logRateLimitExceeded(githubLogin, attempt);

        if (attempt >= MAX_RETRIES) {
            logMaxRetriesReached(githubLogin);
            return false;
        }

        waitForReset(exception);
        return false;
    }

    private void waitForReset(RateLimitExceededException exception) {
        long resetTime = exception.getResetTime();
        long waitTime = resetTime - System.currentTimeMillis();

        try {
            Thread.sleep(Math.max(waitTime + EXTRA_WAIT_MILLIS, MIN_WAIT_MILLIS));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while waiting for rate limit reset", interruptedException);
        }
    }

    private void collectAllData(String githubLogin, String token) {
        collectUserData(githubLogin, token);

        List<Map<String, Object>> repositories = getRepositoryList(githubLogin, token);

        for (Map<String, Object> repository : repositories) {
            collectRepositoryData(repository, token);
        }

        log.info("Completed all data collection for user: {}", githubLogin);
    }

    private void collectUserData(String githubLogin, String token) {
        dataCollectionService.collectUserBasicInfo(githubLogin, token).block();
        log.info("Collected basic info for user: {}", githubLogin);
    }

    private List<Map<String, Object>> getRepositoryList(String githubLogin, String token) {
        try {
            GithubUserBasicRaw userBasic = dataCollectionService
                .collectUserBasicInfo(githubLogin, token)
                .block();

            if (userBasic == null) {
                return List.of();
            }

            return userBasic.getRepositories();
        } catch (Exception exception) {
            log.error("Failed to get repository list for {}", githubLogin, exception);
            return List.of();
        }
    }

    private void collectRepositoryData(Map<String, Object> repository, String token) {
        String repoOwner = extractOwner(repository);
        String repoName = extractName(repository);

        if (repoOwner == null || repoName == null) {
            return;
        }

        collectIssuesAndPRs(repoOwner, repoName, token);
        collectCommits(repoOwner, repoName, token);
    }

    private void collectIssuesAndPRs(String repoOwner, String repoName, String token) {
        dataCollectionService.collectIssues(repoOwner, repoName, token).block();
        dataCollectionService.collectPullRequests(repoOwner, repoName, token).block();
    }

    private void collectCommits(String repoOwner, String repoName, String token) {
        try {
            List<String> commitShas = getCommitShaList(repoOwner, repoName, token);

            for (String sha : commitShas) {
                dataCollectionService.collectCommitDetail(repoOwner, repoName, sha, token).block();
            }
        } catch (Exception exception) {
            log.error("Failed to collect commits for {}/{}", repoOwner, repoName, exception);
        }
    }

    private List<String> getCommitShaList(String repoOwner, String repoName, String token) {
        String uri = String.format("/repos/%s/%s/commits?per_page=100", repoOwner, repoName);

        try {
            List<Map<String, Object>> commits = restApiClient
                .get(uri, token, List.class)
                .block();
            
            if (commits == null) {
                return List.of();
            }
            
            return commits.stream()
                .map(commit -> (String) commit.get("sha"))
                .filter(sha -> sha != null)
                .toList();
        } catch (Exception exception) {
            log.error("Failed to get commit list for {}/{}", repoOwner, repoName, exception);
            return List.of();
        }
    }
    
    private String extractOwner(Map<String, Object> repository) {
        Object owner = repository.get("owner");
        
        if (owner instanceof Map) {
            return (String) ((Map<?, ?>) owner).get("login");
        }
        
        return null;
    }
    
    private String extractName(Map<String, Object> repository) {
        return (String) repository.get("name");
    }
    
    private void logAttempt(String githubLogin, int attempt) {
        log.info("Starting data collection for user: {} (attempt {}/{})", 
            githubLogin, attempt, MAX_RETRIES);
    }
    
    private void logRateLimitWait(String githubLogin, int remaining, long waitTime) {
        log.info("Rate limit low for user {}. Remaining: {}, Waiting {} ms", 
            githubLogin, remaining, waitTime);
    }
    
    private void logSuccess(String githubLogin) {
        log.info("Data collection completed for user: {}", githubLogin);
    }
    
    private void logRateLimitExceeded(String githubLogin, int attempt) {
        log.warn("Rate limit exceeded for user {}. Retry {}/{}", 
            githubLogin, attempt, MAX_RETRIES);
    }
    
    private void logMaxRetriesReached(String githubLogin) {
        log.error("Failed to collect data after {} retries: {}", 
            MAX_RETRIES, githubLogin);
    }
    
    private void logUnexpectedError(String githubLogin, Exception exception) {
        log.error("Unexpected error during data collection for {}: {}", 
            githubLogin, exception.getMessage(), exception);
    }
}
