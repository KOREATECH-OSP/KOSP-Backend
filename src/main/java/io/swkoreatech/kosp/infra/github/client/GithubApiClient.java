package io.swkoreatech.kosp.infra.github.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.swkoreatech.kosp.infra.github.dto.GithubGraphQLResponse;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.infra.github.dto.GithubGraphQLRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
@RequiredArgsConstructor
public class GithubApiClient {

    private final RestClient restClient = RestClient.builder()
        .baseUrl("https://api.github.com/graphql")
        .defaultHeader("User-Agent", "KOSP-Server/1.0")
        .build();

    @org.springframework.beans.factory.annotation.Value("classpath:graphql/github-user-query.graphql")
    private org.springframework.core.io.Resource queryResource;

    public GithubGraphQLResponse getFullUserActivity(String token, String username) {
        if (!isValidToken(token)) {
            log.warn("Github token for user {} is invalid or expired.", username);
            return null;
        }

        String rawQuery;
        try {
            rawQuery = new String(queryResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            log.error("Failed to read GraphQL query file", e);
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }

        GithubGraphQLResponse.UserNode firstUserNode = null;
        java.util.List<GithubGraphQLResponse.RepositoryNode> allRepositories = new java.util.ArrayList<>();
        String cursor = "null";
        
        try {
            while (true) {
                // Determine format: params are (username, cursor)
                // Note: The GraphQL file now expects: user(login: "%s") ... repositories(... after: %s ...)
                String query = rawQuery.formatted(username, cursor);

                org.springframework.http.ResponseEntity<GithubGraphQLResponse> entity = restClient.post()
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new GithubGraphQLRequest(query))
                    .retrieve()
                    .toEntity(GithubGraphQLResponse.class);

                GithubGraphQLResponse response = entity.getBody();

                if (response == null || response.data() == null || response.data().user() == null) {
                    log.error("GraphQL Data is null or partial for user {}", username);
                     // If first request failed, return null. If subsequent, break and return what we have?
                     // Breaking is safer to salvage partial data.
                     break; 
                }

                if (firstUserNode == null) {
                    firstUserNode = response.data().user();
                }

                if (response.data().user().repositories() != null && response.data().user().repositories().nodes() != null) {
                    allRepositories.addAll(response.data().user().repositories().nodes());
                }

                // Check Pagination
                if (response.data().user().repositories() != null 
                    && response.data().user().repositories().pageInfo() != null 
                    && response.data().user().repositories().pageInfo().hasNextPage()) {
                    
                    String endCursor = response.data().user().repositories().pageInfo().endCursor();
                    cursor = "\"" + endCursor + "\""; // Must quote the cursor string for GraphQL
                } else {
                    break;
                }
            } // end while

            if (firstUserNode == null) return null;

            // Reconstruct Response with ALL repositories
            GithubGraphQLResponse.RepositoriesNode combinedRepos =
                new GithubGraphQLResponse.RepositoriesNode(
                    firstUserNode.repositories().totalCount(), 
                    null, // No PageInfo needed for final result
                    allRepositories
                );
            
            GithubGraphQLResponse.UserNode combinedUser =
                new GithubGraphQLResponse.UserNode(
                    firstUserNode.bio(),
                    firstUserNode.company(),
                    firstUserNode.followers(),
                    firstUserNode.following(),
                    firstUserNode.contributionsCollection(),
                    combinedRepos
                );

            return new GithubGraphQLResponse(
                new GithubGraphQLResponse.DataNode(combinedUser)
            );

        } catch (Exception e) {
            log.error("Failed to fetch GitHub activity loop for user {}: {}", username, e.getMessage());
            return null;
        }
    }

    // 간단한 토큰 검증 로직 (필요 시 API 호출하여 유효성 확인 가능)
    private boolean isValidToken(String token) {
        // TODO: 실제 토큰 유효성 검사 로직 (API 호출 등) 필요
        // 현재는 null/empty 체크만 수행
        return token != null && !token.isBlank();
    }
}
