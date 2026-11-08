package io.swkoreatech.kosp.infra.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserPublicInfo(
    Long id,
    String login,
    @JsonProperty("avatar_url") String avatarUrl,
    String email
) {}
