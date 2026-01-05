package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubIssuesRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRsRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubIssuesRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRsRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserBasicRawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubDataCollectionService {

    private final GithubRestApiClient restApiClient;
    private final GithubGraphQLClient graphQLClient;
    
    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubUserBasicRawRepository userBasicRawRepository;
    private final GithubIssuesRawRepository issuesRawRepository;
    private final GithubPRsRawRepository prsRawRepository;

    /**
     * 사용자 기본 정보 수집 (GraphQL)
     */
    public Mono<GithubUserBasicRaw> collectUserBasicInfo(String githubId, String token) {
        return graphQLClient.getUserBasicInfo(githubId, token, Map.class)
            .map(response -> {
                // GraphQL 응답 파싱
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                
                // Document 생성
                GithubUserBasicRaw raw = GithubUserBasicRaw.create(
                    (String) user.get("login"),
                    (String) user.get("name"),
                    (String) user.get("avatarUrl"),
                    (String) user.get("bio"),
                    (String) user.get("company"),
                    (String) user.get("location"),
                    (String) user.get("email"),
                    (String) user.get("createdAt"),
                    getCount(user, "followers"),
                    getCount(user, "following"),
                    getRepositoryCount(user),
                    getRepositories(user),
                    (Map<String, Object>) user.get("contributionsCollection")
                );
                
                // MongoDB 저장
                return userBasicRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected user basic info: {}", githubId))
            .doOnError(error -> log.error("Failed to collect user basic info for {}: {}", githubId, error.getMessage()));
    }

    /**
     * 커밋 상세 정보 수집 (REST API)
     */
    public Mono<GithubCommitDetailRaw> collectCommitDetail(
        String repoOwner,
        String repoName,
        String sha,
        String token
    ) {
        // 이미 수집된 커밋인지 확인
        if (commitDetailRawRepository.existsBySha(sha)) {
            log.debug("Commit {} already collected, skipping", sha);
            return Mono.empty();
        }

        String uri = String.format("/repos/%s/%s/commits/%s", repoOwner, repoName, sha);
        
        return restApiClient.get(uri, token, Map.class)
            .map(response -> {
                // 커밋 상세 정보 파싱
                Map<String, Object> commit = (Map<String, Object>) response.get("commit");
                Map<String, Object> author = (Map<String, Object>) commit.get("author");
                Map<String, Object> committer = (Map<String, Object>) commit.get("committer");
                Map<String, Object> stats = (Map<String, Object>) response.get("stats");
                Object files = response.get("files");
                String message = (String) commit.get("message");
                
                // Document 생성
                GithubCommitDetailRaw raw = GithubCommitDetailRaw.create(
                    sha,
                    repoOwner,
                    repoName,
                    author,
                    committer,
                    stats,
                    files,
                    message
                );
                
                // MongoDB 저장
                return commitDetailRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.debug("Collected commit detail: {}", sha))
            .doOnError(error -> log.error("Failed to collect commit {} for {}/{}: {}", 
                sha, repoOwner, repoName, error.getMessage()));
    }

    /**
     * 레포지토리 이슈 수집 (REST API)
     */
    public Mono<GithubIssuesRaw> collectIssues(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/issues?state=all&per_page=100", repoOwner, repoName);
        
        return restApiClient.get(uri, token, List.class)
            .map(issues -> {
                // Document 생성
                GithubIssuesRaw raw = GithubIssuesRaw.create(
                    repoOwner,
                    repoName,
                    (List<Map<String, Object>>) issues
                );
                
                // MongoDB 저장
                return issuesRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected issues for {}/{}", repoOwner, repoName))
            .doOnError(error -> log.error("Failed to collect issues for {}/{}: {}", 
                repoOwner, repoName, error.getMessage()));
    }

    /**
     * 레포지토리 PR 수집 (REST API)
     */
    public Mono<GithubPRsRaw> collectPullRequests(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/pulls?state=all&per_page=100", repoOwner, repoName);
        
        return restApiClient.get(uri, token, List.class)
            .map(prs -> {
                // Document 생성
                GithubPRsRaw raw = GithubPRsRaw.create(
                    repoOwner,
                    repoName,
                    (List<Map<String, Object>>) prs
                );
                
                // MongoDB 저장
                return prsRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected PRs for {}/{}", repoOwner, repoName))
            .doOnError(error -> log.error("Failed to collect PRs for {}/{}: {}", 
                repoOwner, repoName, error.getMessage()));
    }

    // Helper methods
    private Integer getCount(Map<String, Object> user, String field) {
        Map<String, Object> fieldData = (Map<String, Object>) user.get(field);
        if (fieldData == null) return 0;
        Object totalCount = fieldData.get("totalCount");
        return totalCount instanceof Integer ? (Integer) totalCount : 0;
    }

    private Integer getRepositoryCount(Map<String, Object> user) {
        Map<String, Object> repos = (Map<String, Object>) user.get("repositories");
        if (repos == null) return 0;
        Object totalCount = repos.get("totalCount");
        return totalCount instanceof Integer ? (Integer) totalCount : 0;
    }

    private List<Map<String, Object>> getRepositories(Map<String, Object> user) {
        Map<String, Object> repos = (Map<String, Object>) user.get("repositories");
        if (repos == null) return List.of();
        Object nodes = repos.get("nodes");
        return nodes instanceof List ? (List<Map<String, Object>>) nodes : List.of();
    }
}
