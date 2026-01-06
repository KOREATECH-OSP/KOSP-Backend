package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.model.RateLimitInfo;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCollectionMetadata;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.MongoGithubCollectionMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubDataCollectionRetryService {

    private static final int RATE_LIMIT_THRESHOLD = 100;
    private static final long EXTRA_WAIT_MILLIS = 1000;

    private final GithubDataCollectionService dataCollectionService;
    private final GithubStatisticsService statisticsService;
    private final GithubRateLimitChecker rateLimitChecker;
    private final GithubRestApiClient restApiClient;
    private final TextEncryptor textEncryptor;
    private final MongoGithubCollectionMetadataRepository metadataRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.queue.service.CollectionJobProducer jobProducer;

    /**
     * 데이터 수집 (Event Listener용) - Redis Queue 사용
     */
    @Async
    public void collectWithRetry(String githubLogin, String encryptedToken) {
        try {
            // encryptedToken is actually plain text (decrypted by @Convert)
            // Re-encrypt before storing in Redis
            String reEncryptedToken = textEncryptor.encrypt(encryptedToken);
            
            // Enqueue user collection jobs with re-encrypted token
            jobProducer.enqueueUserCollection(githubLogin, reEncryptedToken);
            
            // Get repository list (use plain text token for API call)
            List<Map<String, Object>> repositories = getRepositoryList(githubLogin, encryptedToken);
            
            for (Map<String, Object> repository : repositories) {
                String repoOwner = extractOwner(repository);
                String repoName = extractName(repository);
                
                if (repoOwner != null && repoName != null) {
                    jobProducer.enqueueRepositoryCollection(repoOwner, repoName, reEncryptedToken);
                }
            }
            
            log.info("Enqueued collection jobs for user: {}", githubLogin);
        } catch (Exception exception) {
            log.error("Failed to enqueue collection jobs for user: {}", githubLogin, exception);
        }
    }

    private void waitIfRateLimitLow(String githubLogin, String token) throws InterruptedException {
        RateLimitInfo rateLimitInfo = rateLimitChecker.checkRateLimit(token);

        if (rateLimitInfo.remaining() >= RATE_LIMIT_THRESHOLD) {
            return;
        }

        long waitTime = rateLimitInfo.getWaitTimeMillis();
        log.info("Rate limit low for user {}. Remaining: {}, Waiting {} ms",
            githubLogin, rateLimitInfo.remaining(), waitTime);
        Thread.sleep(waitTime + EXTRA_WAIT_MILLIS);
    }

    public void collectAllData(String githubLogin, String token) {
        collectUserData(githubLogin, token);
        
        // Event API 수집 (NEW)
        collectUserEvents(githubLogin, token);

        List<Map<String, Object>> repositories = getRepositoryList(githubLogin, token);

        for (Map<String, Object> repository : repositories) {
            collectRepositoryData(repository, token);
        }

        log.info("Completed all data collection for user: {}", githubLogin);
    }
    
    private void collectUserEvents(String githubLogin, String token) {
        dataCollectionService.collectUserEvents(githubLogin, token).block();
        log.info("Collected events for user: {}", githubLogin);
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
        // 메타데이터 확인하여 증분 수집 여부 결정
        GithubCollectionMetadata metadata = metadataRepository
            .findByRepoOwnerAndRepoNameAndCollectionType(repoOwner, repoName, "commits")
            .block();
        
        if (metadata != null) {
            // 증분 수집
            return getCommitsSince(repoOwner, repoName, token, metadata);
        } else {
            // 첫 수집
            return getAllCommitsFirstTime(repoOwner, repoName, token);
        }
    }
    
    private List<String> getCommitsSince(
        String repoOwner,
        String repoName,
        String token,
        GithubCollectionMetadata metadata
    ) {
        String uri = String.format("/repos/%s/%s/commits", repoOwner, repoName);
        
        try {
            List<Map> rawCommits = restApiClient
                .getAllSince(uri, token, metadata.getLastCollectedAt(), Map.class)
                .block();
            
            if (rawCommits == null || rawCommits.isEmpty()) {
                log.info("No new commits for {}/{} since {}", repoOwner, repoName, metadata.getLastCollectedAt());
                return List.of();
            }
            
            // 메타데이터 업데이트
            metadata.updateLastCollected();
            metadataRepository.save(metadata).block();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) (List<?>) rawCommits;
            
            return commits.stream()
                .map(commit -> (String) commit.get("sha"))
                .filter(sha -> sha != null)
                .toList();
        } catch (Exception exception) {
            log.error("Failed to get incremental commits for {}/{}", repoOwner, repoName, exception);
            return List.of();
        }
    }
    
    private List<String> getAllCommitsFirstTime(String repoOwner, String repoName, String token) {
        String uri = String.format("/repos/%s/%s/commits", repoOwner, repoName);

        try {
            List<Map> rawCommits = restApiClient
                .getAllWithPagination(uri, token, Map.class)
                .block();
            
            if (rawCommits == null) {
                return List.of();
            }
            
            // 메타데이터 생성
            GithubCollectionMetadata metadata = GithubCollectionMetadata.create(
                null, repoOwner, repoName, "commits"
            );
            metadataRepository.save(metadata).block();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) (List<?>) rawCommits;
            
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
}

