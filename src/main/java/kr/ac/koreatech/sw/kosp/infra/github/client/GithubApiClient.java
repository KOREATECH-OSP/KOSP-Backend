package kr.ac.koreatech.sw.kosp.infra.github.client;

import kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLRequest;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;



@Slf4j
@Component
@RequiredArgsConstructor
public class GithubApiClient {

    private final RestClient restClient = RestClient.builder()
        .baseUrl("https://api.github.com/graphql")
        .build();

    @org.springframework.beans.factory.annotation.Value("classpath:graphql/github-user-query.graphql")
    private org.springframework.core.io.Resource queryResource;

    public kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse getFullUserActivity(String token, String username) {
        if (!isValidToken(token)) {
            log.warn("Github token for user {} is invalid or expired.", username);
            return null;
        }

        String query;
        try {
            query = new String(queryResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            log.error("Failed to read GraphQL query file", e);
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }
        
        query = query.formatted(username);

        try {
            org.springframework.http.ResponseEntity<kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse> entity = restClient.post()
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GithubGraphQLRequest(query))
                .retrieve()
                .toEntity(kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse.class);
            
            // Log Rate Limit
            String remaining = entity.getHeaders().getFirst("X-RateLimit-Remaining");
            if (remaining != null) {
                log.info("GitHub API Rate Limit Remaining for {}: {}", username, remaining);
                try {
                    int remainingCount = Integer.parseInt(remaining);
                    if (remainingCount < 100) {
                        log.warn("Warning: GitHub API Rate Limit is running low ({}) for user {}", remainingCount, username);
                    }
                } catch (NumberFormatException ignored) {}
            }

            kr.ac.koreatech.sw.kosp.infra.github.dto.GithubGraphQLResponse response = entity.getBody();
            
            if (response != null && response.data() == null) {
                // Check if errors exist? Structure of error response might be different from success.
                // RestClient might throw if body mapping fails or returns partial.
                // For now, assuming standard response.
                log.error("GraphQL Data is null for user {}", username);
                return null;
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to fetch GitHub activity for user {}: {}", username, e.getMessage());
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
