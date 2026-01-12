package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.MongoGithubCollectionMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubCommitCollectionService {
    
    private final GithubRestApiClient restApiClient;
    private final GithubDataCollectionService dataCollectionService;
    private final MongoGithubCollectionMetadataRepository metadataRepository;
    private final FailureAnalyzer failureAnalyzer;
    
    /**
     * 레포지토리의 모든 커밋 수집 (개별 문서 저장)
     */
    public Mono<Long> collectAllCommits(
        String repoOwner,
        String repoName,
        String githubLogin,  // ✅ 추가: Author 필터링용
        String token
    ) {
        String context = String.format("%s/%s/commits", repoOwner, repoName);
        
        return getCommitShaList(repoOwner, repoName, githubLogin, token)
            .flatMapMany(reactor.core.publisher.Flux::fromIterable)
            .flatMap(sha -> 
                dataCollectionService.collectCommitDetail(repoOwner, repoName, sha, token)
                    .map(commit -> 1)  // 새로 수집된 커밋
                    .defaultIfEmpty(1)  // 이미 수집된 커밋도 카운트
                    .onErrorResume(e -> {
                        // 실패 분석 및 기록
                        var failureType = failureAnalyzer.classifyFailure((Exception) e);
                        failureAnalyzer.recordFailure(context, failureType, (Exception) e);
                        
                        // 재시도 가능한 에러는 예외를 전파하여 Worker가 재시도하도록 함
                        if (failureAnalyzer.isRetryable(failureType)) {
                            log.warn("Retryable error for commit {} in {}/{}: {} - {}", 
                                sha, repoOwner, repoName, failureType, e.getMessage());
                            return Mono.error(e);  // ✅ 에러 전파
                        }
                        
                        // 재시도 불가능한 에러만 스킵 (404 등)
                        log.debug("Skipping non-retryable error for commit {} in {}/{}: {} - {}", 
                            sha, repoOwner, repoName, failureType, e.getMessage());
                        return Mono.just(0);
                    }),
                15  // 최대 15개 동시 처리 (GitHub secondary rate limit 회피)
            )
            .reduce(0L, (acc, val) -> acc + val)
            .doOnSuccess(count -> {
                log.info("✅ Collected {} commits for {}/{}", count, repoOwner, repoName);
                // 실패 통계 로깅
                failureAnalyzer.logFailureStatistics(context);
            })
            .doOnError(error -> {
                // 전체 작업 실패 - 명확한 로깅
                log.error("❌ CRITICAL: Failed to collect commits for {}/{}: {}", 
                    repoOwner, repoName, error.getMessage());
                var failureType = failureAnalyzer.classifyFailure((Exception) error);
                failureAnalyzer.recordFailure(context, failureType, (Exception) error);
                failureAnalyzer.logFailureStatistics(context);
                // ❌ onErrorResume 제거 - Worker가 재시도하도록 에러 전파
            });
    }
    
    /**
     * 커밋 SHA 리스트 가져오기 (Author 필터링 적용)
     * 
     * Reference: SKKU-OSP github.py Line 431-456
     * 
     * @param repoOwner 저장소 소유자
     * @param repoName 저장소 이름
     * @param githubLogin 필터링할 사용자의 GitHub 로그인 ID
     * @param token GitHub API 토큰
     * @return 필터링된 커밋 SHA 리스트
     */
    private Mono<List<String>> getCommitShaList(
        String repoOwner, 
        String repoName,
        String githubLogin,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/commits", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(rawCommits -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> commits = (List<Map<String, Object>>) (List<?>) rawCommits;
                
                return commits.stream()
                    // ✅ Author 필터링 추가
                    .filter(commit -> {
                        // Author 정보 추출
                        @SuppressWarnings("unchecked")
                        Map<String, Object> author = (Map<String, Object>) commit.get("author");
                        
                        // Author가 null이면 제외
                        if (author == null) {
                            log.debug("Commit {} has no author information", commit.get("sha"));
                            return false;
                        }
                        
                        // Author login 추출
                        String login = (String) author.get("login");
                        
                        // Login이 null이면 제외
                        if (login == null) {
                            log.debug("Commit {} author has no login", commit.get("sha"));
                            return false;
                        }
                        
                        // githubLogin과 일치하는지 확인
                        boolean matches = githubLogin.equals(login);
                        
                        if (matches) {
                            log.debug("✅ Commit {} matches author {}", commit.get("sha"), githubLogin);
                        } else {
                            log.trace("❌ Commit {} author {} does not match {}", 
                                commit.get("sha"), login, githubLogin);
                        }
                        
                        return matches;
                    })
                    .map(commit -> (String) commit.get("sha"))
                    .filter(sha -> sha != null)
                    .toList();
            })
            .doOnSuccess(shas -> {
                log.info("✅ Filtered {} commits for author {} in {}/{}", 
                    shas.size(), githubLogin, repoOwner, repoName);
            });
    }
}
