package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.client.rest.GithubRestApiClient;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
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
    
    // New individual document repositories
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubIssueRawRepository issueRawRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRRawRepository prRawRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitRawRepository commitRawRepository;
    
    // Features 10-11 repositories
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserFollowingRepository userFollowingRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserStarredRepository userStarredRepository;
    
    // Feature 10-11 repositories
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserFollowingRepository userFollowingRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubUserStarredRepository userStarredRepository;

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
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                
                // Feature 12: Total Stars 계산 (외부 API 값 합산)
                int totalStars = allRepos.stream()
                    .mapToInt(repo -> {
                        Object stargazers = repo.get("stargazerCount");
                        if (stargazers instanceof Integer) {
                            return (Integer) stargazers;
                        }
                        return 0;
                    })
                    .sum();
                
                log.info("Calculated total stars for {}: {}", githubId, totalStars);
                
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
                    (Map<String, Object>) user.get("contributionsCollection"),
                    totalStars  // Feature 12
                );
                
                return userBasicRawRepository.save(raw);
            })
            .doOnSuccess(saved -> log.info("Collected user basic info with {} repositories and {} stars: {}", 
                allRepos.size(), saved.getTotalStars(), githubId))
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
     * 레포지토리 이슈 수집 (개별 문서 저장)
     */
    public Mono<Long> collectIssues(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/issues?state=all", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .flatMapMany(rawIssues -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> issues = (List<Map<String, Object>>) (List<?>) rawIssues;
                
                return reactor.core.publisher.Flux.fromIterable(issues)
                    .map(issueData -> {
                        Integer issueNumber = (Integer) issueData.get("number");
                        return kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubIssueRaw.create(
                            repoOwner,
                            repoName,
                            issueNumber,
                            issueData
                        );
                    });
            })
            .buffer(100) // Batch save 100 at a time
            .flatMap(batch -> issueRawRepository.saveAll(batch).collectList())
            .count()
            .doOnSuccess(count -> log.info("Collected {} issues for {}/{}", count, repoOwner, repoName))
            .doOnError(error -> log.error("Failed to collect issues for {}/{}: {}", 
                repoOwner, repoName, error.getMessage()));
    }

    /**
     * 레포지토리 PR 수집 (개별 문서 저장)
     */
    public Mono<Long> collectPullRequests(
        String repoOwner,
        String repoName,
        String token
    ) {
        String uri = String.format("/repos/%s/%s/pulls?state=all", repoOwner, repoName);
        
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .flatMapMany(rawPrs -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> prs = (List<Map<String, Object>>) (List<?>) rawPrs;
                
                return reactor.core.publisher.Flux.fromIterable(prs)
                    .map(prData -> {
                        Integer prNumber = (Integer) prData.get("number");
                        return kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRRaw.create(
                            repoOwner,
                            repoName,
                            prNumber,
                            prData
                        );
                    });
            })
            .buffer(100) // Batch save 100 at a time
            .flatMap(batch -> prRawRepository.saveAll(batch).collectList())
            .count()
            .doOnSuccess(count -> log.info("Collected {} PRs for {}/{}", count, repoOwner, repoName))
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
    
    // ========== Feature 7: Repository API Data ==========
    
    /**
     * Contributors 수집 (Feature 7)
     */
    public Mono<List<String>> collectContributors(String owner, String repo, String token) {
        String uri = String.format("/repos/%s/%s/contributors", owner, repo);
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(contributors -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) contributors;
                return list.stream()
                    .map(c -> (String) c.get("login"))
                    .filter(login -> login != null)
                    .toList();
            })
            .doOnSuccess(logins -> log.info("Collected {} contributors for {}/{}", logins.size(), owner, repo))
            .onErrorResume(e -> {
                log.warn("Failed to collect contributors for {}/{}: {}", owner, repo, e.getMessage());
                return Mono.just(List.of());
            });
    }
    
    /**
     * Releases 수집 (Feature 7)
     */
    public Mono<Map<String, Object>> collectReleases(String owner, String repo, String token) {
        String uri = String.format("/repos/%s/%s/releases", owner, repo);
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .map(releases -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) releases;
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("releaseCount", list.size());
                if (!list.isEmpty()) {
                    String latestName = (String) list.get(0).get("name");
                    if (latestName != null && latestName.length() > 45) {
                        latestName = latestName.substring(0, 45);
                    }
                    result.put("latestRelease", latestName);
                }
                return result;
            })
            .doOnSuccess(r -> log.info("Collected {} releases for {}/{}", r.get("releaseCount"), owner, repo))
            .onErrorResume(e -> {
                log.warn("Failed to collect releases for {}/{}: {}", owner, repo, e.getMessage());
                return Mono.just(Map.of("releaseCount", 0));
            });
    }
    
    /**
     * README 확인 (Feature 7)
     */
    public Mono<Integer> checkReadme(String owner, String repo, String token) {
        String uri = String.format("/repos/%s/%s/contents", owner, repo);
        return restApiClient.get(uri, token, List.class)
            .map(contents -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) contents;
                return list.stream()
                    .filter(file -> {
                        String name = (String) file.get("name");
                        return name != null && name.toLowerCase().contains("readme");
                    })
                    .findFirst()
                    .map(file -> (Integer) file.get("size"))
                    .orElse(0);
            })
            .onErrorResume(e -> {
                log.warn("Failed to check README for {}/{}: {}", owner, repo, e.getMessage());
                return Mono.just(0);
            });
    }
    
    // ========== Feature 10: User Following ==========
    
    /**
     * Following 수집 (Feature 10)
     */
    public Mono<List<kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserFollowing>> collectFollowing(
        String githubId, String token
    ) {
        String uri = String.format("/users/%s/following", githubId);
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .flatMapMany(following -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) following;
                return reactor.core.publisher.Flux.fromIterable(list)
                    .map(user -> kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserFollowing
                        .create(githubId, (String) user.get("login")))
                    .filter(uf -> !userFollowingRepository.existsByGithubIdAndFollowingId(
                        uf.getGithubId(), uf.getFollowingId()));
            })
            .collectList()
            .flatMap(list -> {
                if (list.isEmpty()) return Mono.just(list);
                return Mono.fromCallable(() -> userFollowingRepository.saveAll(list))
                    .map(saved -> list);
            })
            .doOnSuccess(list -> log.info("Collected {} following for {}", list.size(), githubId))
            .onErrorResume(e -> {
                log.warn("Failed to collect following for {}: {}", githubId, e.getMessage());
                return Mono.just(List.of());
            });
    }
    
    // ========== Feature 11: User Starred ==========
    
    /**
     * Starred 수집 (Feature 11)
     */
    public Mono<List<kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserStarred>> collectStarred(
        String githubId, String token
    ) {
        String uri = String.format("/users/%s/starred", githubId);
        return restApiClient.getAllWithPagination(uri, token, Map.class)
            .flatMapMany(starred -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) (List<?>) starred;
                return reactor.core.publisher.Flux.fromIterable(list)
                    .map(repo -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> owner = (Map<String, Object>) repo.get("owner");
                        return kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserStarred
                            .create(githubId, (String) owner.get("login"), (String) repo.get("name"));
                    })
                    .filter(us -> !userStarredRepository.existsByGithubIdAndStarredRepoOwnerAndStarredRepoName(
                        us.getGithubId(), us.getStarredRepoOwner(), us.getStarredRepoName()));
            })
            .collectList()
            .flatMap(list -> {
                if (list.isEmpty()) return Mono.just(list);
                return Mono.fromCallable(() -> userStarredRepository.saveAll(list))
                    .map(saved -> list);
            })
            .doOnSuccess(list -> log.info("Collected {} starred repos for {}", list.size(), githubId))
            .onErrorResume(e -> {
                log.warn("Failed to collect starred for {}: {}", githubId, e.getMessage());
                return Mono.just(List.of());
            });
    }
}

