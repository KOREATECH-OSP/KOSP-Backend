package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubIssuesRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRsRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserEventsRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubIssuesRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRsRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserBasicRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.MongoGithubUserEventsRawRepository;
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
    private final MongoGithubUserEventsRawRepository eventsRawRepository;

    /**
     * 사용자 기본 정보 수집 (GraphQL Pagination)
     */
    public Mono<GithubUserBasicRaw> collectUserBasicInfo(String githubId, String token) {
        return Mono.defer(() -> {
            List<Map<String, Object>> allRepos = new ArrayList<>();
            return collectAllRepositories(githubId, token, null, allRepos)
                .then(Mono.defer(() -> buildAndSaveUserBasicRaw(githubId, token, allRepos)));
        });
    }

    private Mono<Void> collectAllRepositories(
        String githubId,
        String token,
        String cursor,
        List<Map<String, Object>> accumulator
    ) {
        return graphQLClient.getUserBasicInfoPaginated(githubId, cursor, token, Map.class)
            .flatMap(response -> {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                Map<String, Object> reposConnection = (Map<String, Object>) user.get("repositories");
                
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) reposConnection.get("nodes");
                if (nodes != null && !nodes.isEmpty()) {
                    accumulator.addAll(nodes);
                }
                
                Map<String, Object> pageInfo = (Map<String, Object>) reposConnection.get("pageInfo");
                Boolean hasNextPage = (Boolean) pageInfo.get("hasNextPage");
                
                if (hasNextPage == null || !hasNextPage) {
                    return Mono.empty();
                }
                
                String endCursor = (String) pageInfo.get("endCursor");
                return collectAllRepositories(githubId, token, endCursor, accumulator);
            })
            .then();
    }

    private Mono<GithubUserBasicRaw> buildAndSaveUserBasicRaw(
        String githubId,
        String token,
        List<Map<String, Object>> allRepos
    ) {
        return graphQLClient.getUserBasicInfo(githubId, token, Map.class)
            .map(response -> {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                
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
                    allRepos.size(),
                    allRepos,
                    (Map<String, Object>) user.get("contributionsCollection")
                );
                
                return userBasicRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected user basic info with {} repositories: {}", allRepos.size(), githubId))
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
     * 레포지토리 이슈 수집 (REST API Pagination)
     */
    public Mono<GithubIssuesRaw> collectIssues(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/issues?state=all", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(rawIssues -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) (List<?>) rawIssues;
                
                GithubIssuesRaw raw = GithubIssuesRaw.create(
                    repoOwner,
                    repoName,
                    issues
                );
                
                return issuesRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected {} issues for {}/{}", 
                saved.getIssues().size(), repoOwner, repoName))
            .doOnError(error -> log.error("Failed to collect issues for {}/{}: {}", 
                repoOwner, repoName, error.getMessage()));
    }

    /**
     * 레포지토리 PR 수집 (REST API Pagination)
     */
    public Mono<GithubPRsRaw> collectPullRequests(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/pulls?state=all", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(rawPrs -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> prs = (List<Map<String, Object>>) (List<?>) rawPrs;
                
                GithubPRsRaw raw = GithubPRsRaw.create(
                    repoOwner,
                    repoName,
                    prs
                );
                
                return prsRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected {} PRs for {}/{}", 
                saved.getPullRequests().size(), repoOwner, repoName))
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

    /**
     * User Events 수집 (GitHub Event API)
     */
    public Mono<GithubUserEventsRaw> collectUserEvents(String githubLogin, String token) {
        String uri = String.format("/users/%s/events", githubLogin);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(rawEvents -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> events = (List<Map<String, Object>>) (List<?>) rawEvents;
                
                return GithubUserEventsRaw.create(githubLogin, events);
            })
            .flatMap(eventsRawRepository::save)
            .doOnSuccess(saved -> log.info("Collected {} events for user: {}", 
                saved.getEvents().size(), githubLogin))
            .doOnError(error -> log.error("Failed to collect events for user: {}", 
                githubLogin, error));
    }
}

