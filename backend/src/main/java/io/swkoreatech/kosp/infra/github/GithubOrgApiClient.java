package io.swkoreatech.kosp.infra.github;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMember;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMembership;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgRepo;
import io.swkoreatech.kosp.infra.github.dto.GithubUserPublicInfo;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GithubOrgApiClient {

    private static final String BASE_URL = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";
    private static final int PER_PAGE = 100;

    private final WebClient webClient;

    public GithubOrgApiClient() {
        this.webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", GITHUB_API_VERSION)
            .build();
    }

    public List<GithubOrgMembership> getMyOrgMemberships(String token) {
        List<GithubOrgMembership> result = webClient.get()
            .uri("/user/memberships/orgs?per_page=" + PER_PAGE + "&state=active")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .onStatus(
                status -> status.value() == 403 || status.value() == 401 || status.value() == 429,
                response -> Mono.error(new GlobalException(ExceptionMessage.GITHUB_REAUTH_REQUIRED))
            )
            .bodyToMono(new ParameterizedTypeReference<List<GithubOrgMembership>>() {})
            .doOnError(error -> log.warn("GitHub 조직 멤버십 조회 실패: {}", error.getMessage()))
            .block();
        if (result != null) {
            result.forEach(m -> log.info("[DEBUG] org={}, role={}, state={}",
                m.organization().login(), m.role(), m.state()));
        }
        return result;
    }

    public List<GithubOrgMember> getOrgMembers(String token, String orgName) {
        return webClient.get()
            .uri("/orgs/{org}/members?per_page=" + PER_PAGE, orgName)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .onStatus(
                status -> status.value() == 403 || status.value() == 429,
                response -> Mono.error(new GlobalException(ExceptionMessage.GITHUB_REAUTH_REQUIRED))
            )
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.error(new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND))
            )
            .bodyToMono(new ParameterizedTypeReference<List<GithubOrgMember>>() {})
            .doOnError(error -> log.warn("GitHub 조직 멤버 조회 실패 [{}]: {}", orgName, error.getMessage()))
            .block();
    }

    public String getUserEmail(String token, String username) {
        GithubUserPublicInfo info = webClient.get()
            .uri("/users/{username}", username)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .bodyToMono(GithubUserPublicInfo.class)
            .doOnError(error -> log.warn("GitHub 유저 정보 조회 실패 [{}]: {}", username, error.getMessage()))
            .onErrorReturn(new GithubUserPublicInfo(null, username, null, null))
            .block();
        return info != null ? info.email() : null;
    }

    public List<GithubOrgRepo> getOrgRepos(String token, String orgName) {
        return webClient.get()
            .uri("/orgs/{org}/repos?per_page=" + PER_PAGE + "&type=all", orgName)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .onStatus(
                status -> status.value() == 403 || status.value() == 429,
                response -> Mono.error(new GlobalException(ExceptionMessage.GITHUB_REAUTH_REQUIRED))
            )
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.error(new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND))
            )
            .bodyToMono(new ParameterizedTypeReference<List<GithubOrgRepo>>() {})
            .doOnError(error -> log.warn("GitHub 조직 저장소 조회 실패 [{}]: {}", orgName, error.getMessage()))
            .block();
    }
}
