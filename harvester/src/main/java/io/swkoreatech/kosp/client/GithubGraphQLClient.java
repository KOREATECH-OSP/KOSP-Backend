package io.swkoreatech.kosp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

/**
 * GitHub GraphQL API 클라이언트.
 *
 * <p>classpath에서 GraphQL 쿼리 파일을 로드하고,
 * WebClient를 사용하여 GitHub GraphQL API에 요청을 보낸다.
 * 기여 저장소, PR, 이슈, 커밋 등의 데이터를 조회한다.
 */
@Slf4j
@Component
public class GithubGraphQLClient {

    private final WebClient webClient;
    private final ResourceLoader resourceLoader;

    private String userBasicInfoQuery;
    private String userContributionsQuery;
    private String contributedReposQuery;
    private String userPullRequestsQuery;
    private String userIssuesQuery;
    private String repositoryCommitsQuery;

    /**
     * GithubGraphQLClient를 생성하고 GraphQL 쿼리를 로드한다.
     *
     * @param graphqlUrl     GitHub GraphQL API URL
     * @param resourceLoader 쿼리 파일 로드용 리소스 로더
     */
    public GithubGraphQLClient(
        @Value("${github.api.graphql-url}") String graphqlUrl,
        ResourceLoader resourceLoader
    ) {
        this.webClient = createWebClient(graphqlUrl);
        this.resourceLoader = resourceLoader;
        loadQueries();
    }

    private WebClient createWebClient(String baseUrl) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("graphql-pool")
            .maxConnections(60)
            .pendingAcquireMaxCount(500)
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
            .responseTimeout(Duration.ofSeconds(60))
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));
            });

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    private void loadQueries() {
        try {
            userBasicInfoQuery = loadQuery("classpath:graphql/user-basic-info.graphql");
            userContributionsQuery = loadQuery("classpath:graphql/user-contributions.graphql");
            contributedReposQuery = loadQuery("classpath:graphql/contributed-repositories.graphql");
            userPullRequestsQuery = loadQuery("classpath:graphql/user-pull-requests.graphql");
            userIssuesQuery = loadQuery("classpath:graphql/user-issues.graphql");
            repositoryCommitsQuery = loadQuery("classpath:graphql/repository-commits.graphql");
            log.info("GraphQL queries loaded successfully");
        } catch (IOException e) {
            log.warn("GraphQL queries not found: {}", e.getMessage());
        }
    }

    private String loadQuery(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + path);
        }
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * GraphQL 쿼리를 실행한다.
     *
     * @param <T>          응답 타입
     * @param query        GraphQL 쿼리 문자열
     * @param variables    쿼리 변수 맵
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 응답 결과를 담은 Mono
     */
    public <T> Mono<T> query(String query, Map<String, Object> variables, String token, Class<T> responseType) {
        Map<String, Object> requestBody = buildRequestBody(query, variables);

        return webClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .retryWhen(createRetrySpec())
            .doOnError(error -> log.warn("GraphQL query failed: {}", error.getMessage()));
    }

    private Map<String, Object> buildRequestBody(String query, Map<String, Object> variables) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        if (variables != null && !variables.isEmpty()) {
            requestBody.put("variables", variables);
        }
        return requestBody;
    }

    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(2))
            .maxBackoff(Duration.ofSeconds(30))
            .jitter(0.5)
            .filter(throwable ->
                throwable instanceof WebClientResponseException.BadGateway ||
                throwable instanceof WebClientResponseException.ServiceUnavailable ||
                throwable instanceof WebClientResponseException.TooManyRequests ||
                throwable instanceof java.util.concurrent.TimeoutException ||
                throwable instanceof io.netty.handler.timeout.ReadTimeoutException ||
                (throwable.getMessage() != null && (
                    throwable.getMessage().contains("prematurely closed") ||
                    throwable.getMessage().contains("Connection reset") ||
                    throwable.getMessage().contains("Connection refused")
                ))
            );
    }

    /**
     * 특정 기간 동안 사용자가 기여한 저장소 목록을 조회한다.
     *
     * @param <T>          응답 타입
     * @param login        GitHub 로그인 이름
     * @param from         조회 시작 시각 (ISO 형식)
     * @param to           조회 종료 시각 (ISO 형식)
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 기여 저장소 응답을 담은 Mono
     */
    public <T> Mono<T> getContributedRepos(
        String login,
        String from,
        String to,
        String token,
        Class<T> responseType
    ) {
        Map<String, Object> variables = Map.of(
            "login", login,
            "from", from,
            "to", to
        );
        return query(contributedReposQuery, variables, token, responseType);
    }

    /**
     * 사용자의 풀 리퀘스트 목록을 페이지네이션하여 조회한다.
     *
     * @param <T>          응답 타입
     * @param login        GitHub 로그인 이름
     * @param cursor       페이지네이션 커서 (첫 페이지는 null)
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return PR 목록 응답을 담은 Mono
     */
    public <T> Mono<T> getUserPullRequests(String login, String cursor, String token, Class<T> responseType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        if (cursor != null) {
            variables.put("after", cursor);
        }
        return query(userPullRequestsQuery, variables, token, responseType);
    }

    /**
     * 사용자의 이슈 목록을 페이지네이션하여 조회한다.
     *
     * @param <T>          응답 타입
     * @param login        GitHub 로그인 이름
     * @param cursor       페이지네이션 커서 (첫 페이지는 null)
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 이슈 목록 응답을 담은 Mono
     */
    public <T> Mono<T> getUserIssues(String login, String cursor, String token, Class<T> responseType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        if (cursor != null) {
            variables.put("after", cursor);
        }
        return query(userIssuesQuery, variables, token, responseType);
    }

    /**
     * 사용자의 기본 정보와 소유 저장소 목록을 조회한다.
     *
     * @param <T>          응답 타입
     * @param login        GitHub 로그인 이름
     * @param cursor       페이지네이션 커서 (첫 페이지는 null)
     * @param token        GitHub 인증 토큰
     * @param responseType 응답 클래스 타입
     * @return 사용자 기본 정보 응답을 담은 Mono
     */
    public <T> Mono<T> getUserBasicInfo(
        String login,
        String cursor,
        String token,
        Class<T> responseType
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("login", login);
        if (cursor != null) {
            variables.put("after", cursor);
        }
        return query(userBasicInfoQuery, variables, token, responseType);
    }

    /**
     * 특정 저장소에서 지정된 작성자의 커밋 목록을 조회한다.
     *
     * @param <T>          응답 타입
     * @param owner        저장소 소유자
     * @param name         저장소 이름
     * @param authorId     작성자의 GitHub 노드 ID
     * @param cursor       페이지네이션 커서 (첫 페이지는 null)
     * @param token        GitHub 인증 토큰
     * @param pageSize     페이지 크기
     * @param responseType 응답 클래스 타입
     * @return 커밋 목록 응답을 담은 Mono
     */
    public <T> Mono<T> getRepositoryCommits(
        String owner,
        String name,
        String authorId,
        String cursor,
        String token,
        int pageSize,
        Class<T> responseType
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("owner", owner);
        variables.put("name", name);
        variables.put("authorId", authorId);
        variables.put("first", pageSize);
        if (cursor != null) {
            variables.put("after", cursor);
        }
        return query(repositoryCommitsQuery, variables, token, responseType);
    }
}
