package io.swkoreatech.kosp.infra.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubOrgSummary(
    Long id,
    String login,
    @JsonProperty("avatar_url") String avatarUrl
) {}
