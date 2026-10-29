package io.swkoreatech.kosp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub REST API의 사용자 소속 조직 응답 DTO.
 *
 * <p>GET /user/orgs 응답을 매핑한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOrgResponse {
    private String login;
}
