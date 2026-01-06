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
    
    /**
     * 레포지토리의 모든 커밋 수집 (개별 문서 저장)
     */
    public Mono<Long> collectAllCommits(
        String repoOwner,
        String repoName,
        String token
    ) {
        return getCommitShaList(repoOwner, repoName, token)
            .flatMapMany(reactor.core.publisher.Flux::fromIterable)
            .flatMap(sha -> 
                dataCollectionService.collectCommitDetail(repoOwner, repoName, sha, token)
                    .onErrorResume(e -> {
                        log.warn("Failed to collect commit {}: {}", sha, e.getMessage());
                        return Mono.empty();
                    })
            )
            .count()
            .doOnSuccess(count -> log.info("Collected {} commits for {}/{}", count, repoOwner, repoName))
            .doOnError(error -> log.error("Failed to collect commits for {}/{}: {}", 
                repoOwner, repoName, error.getMessage()));
    }
    
    /**
     * 커밋 SHA 리스트 가져오기
     */
    private Mono<List<String>> getCommitShaList(String repoOwner, String repoName, String token) {
        String uri = String.format("/repos/%s/%s/commits", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(rawCommits -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> commits = (List<Map<String, Object>>) (List<?>) rawCommits;
                
                return commits.stream()
                    .map(commit -> (String) commit.get("sha"))
                    .filter(sha -> sha != null)
                    .toList();
            });
    }
}
