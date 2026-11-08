package io.swkoreatech.kosp.infra.github.dto;

public record GithubOrgMembership(
    String role,
    String state,
    GithubOrgSummary organization
) {}
