package io.swkoreatech.kosp.infra.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubOrgRepo(
    Long id,
    String name,
    @JsonProperty("full_name") String fullName,
    @JsonProperty("html_url") String htmlUrl,
    String visibility
) {}
