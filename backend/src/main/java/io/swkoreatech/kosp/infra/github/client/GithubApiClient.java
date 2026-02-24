package io.swkoreatech.kosp.infra.github.client;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.infra.github.dto.GithubGraphQLRequest;
import io.swkoreatech.kosp.infra.github.dto.GithubGraphQLResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
            rawQuery = new String(
                    queryResource.getInputStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (java.io.IOException e) {
            log.error("Failed to read GraphQL query file", e);
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }

        GithubGraphQLResponse.UserNode firstUserNode = null;
        java.util.List<GithubGraphQLResponse.RepositoryNode> allRepositories = new java.util.ArrayList<>();
        String cursor = "null";

        try {
            while (true) {
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
                    break;
                }

                if (firstUserNode == null) {
                    firstUserNode = response.data().user();
                }

                if (response.data().user().repositories() != null
                        && response.data().user().repositories().nodes() != null) {
                    allRepositories.addAll(response.data().user().repositories().nodes());
                }

                if (response.data().user().repositories() != null
                    && response.data().user().repositories().pageInfo() != null
                    && response.data().user().repositories().pageInfo().hasNextPage()) {

                    String endCursor = response.data().user().repositories().pageInfo().endCursor();
                    cursor = "\"" + endCursor + "\"";
                } else {
                    break;
                }
            }

            if (firstUserNode == null) {
                return null;
            }

            GithubGraphQLResponse.RepositoriesNode combinedRepos =
                new GithubGraphQLResponse.RepositoriesNode(
                    firstUserNode.repositories().totalCount(),
                    null,
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

    private boolean isValidToken(String token) {
        return token != null && !token.isBlank();
    }
}
