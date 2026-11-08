package io.swkoreatech.kosp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * GitHub REST API의 조직 레포지토리 응답 DTO.
 *
 * <p>GET /orgs/{org}/repos 응답을 매핑한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOrgRepoResponse {

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("private")
    private boolean isPrivate;
}
