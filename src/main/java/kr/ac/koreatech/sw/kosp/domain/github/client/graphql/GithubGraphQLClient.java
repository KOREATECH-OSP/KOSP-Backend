package kr.ac.koreatech.sw.kosp.domain.github.client.graphql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GithubGraphQLClient {

    private final WebClient webClient;
    private final ResourceLoader resourceLoader;
    
    private String userBasicInfoQuery;
    private String userBasicInfoPaginatedQuery;
    private String userContributionsQuery;
    private String repositoryInfoQuery;

    public GithubGraphQLClient(
        @Value("${github.api.graphql-url}") String graphqlUrl,
        ResourceLoader resourceLoader
    ) {
        this.webClient = WebClient.builder()
            .baseUrl(graphqlUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.resourceLoader = resourceLoader;
        loadQueries();
    }

    private void loadQueries() {
        try {
            userBasicInfoQuery = loadQuery("classpath:graphql/user-basic-info.graphql");
            userBasicInfoPaginatedQuery = loadQuery("classpath:graphql/user-basic-info-paginated.graphql");
            userContributionsQuery = loadQuery("classpath:graphql/user-contributions.graphql");
            repositoryInfoQuery = loadQuery("classpath:graphql/repository-info.graphql");
            log.info("GraphQL queries loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load GraphQL queries", e);
            throw new RuntimeException("Failed to load GraphQL queries", e);
        }
    }

    private String loadQuery(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public <T> Mono<T> query(String query, String token, Class<T> responseType) {
        Map<String, Object> requestBody = Map.of("query", query);

        return webClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("GraphQL query executed successfully"))
            .doOnError(error -> log.error("GraphQL query failed: {}", error.getMessage()));
    }

    public <T> Mono<T> query(String query, Map<String, Object> variables, String token, Class<T> responseType) {
        Map<String, Object> requestBody = Map.of(
            "query", query,
            "variables", variables
        );

        return webClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .doOnSuccess(response -> log.debug("GraphQL query with variables executed successfully"))
            .doOnError(error -> log.error("GraphQL query with variables failed: {}", error.getMessage()));
    }

    /**
     * 사용자 기본 정보 조회
     */
    public <T> Mono<T> getUserBasicInfo(String login, String token, Class<T> responseType) {
        Map<String, Object> variables = Map.of("login", login);
        return query(userBasicInfoQuery, variables, token, responseType);
    }

    /**
     * 사용자 기여 통계 조회
     */
    public <T> Mono<T> getUserContributions(
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
        return query(userContributionsQuery, variables, token, responseType);
    }

    /**
     * 저장소 정보 조회
     */
    public <T> Mono<T> getRepositoryInfo(
        String owner,
        String name,
        String token,
        Class<T> responseType
    ) {
        Map<String, Object> variables = Map.of(
            "owner", owner,
            "name", name
        );
        return query(repositoryInfoQuery, variables, token, responseType);
    }

    /**
     * 사용자 기본 정보 조회 (Pagination 지원)
     */
    public <T> Mono<T> getUserBasicInfoPaginated(
        String login,
        String cursor,
        String token,
        Class<T> responseType
    ) {
        Map<String, Object> variables = cursor == null ?
            Map.of("login", login) :
            Map.of("login", login, "cursor", cursor);
        
        return query(userBasicInfoPaginatedQuery, variables, token, responseType);
    }
}
