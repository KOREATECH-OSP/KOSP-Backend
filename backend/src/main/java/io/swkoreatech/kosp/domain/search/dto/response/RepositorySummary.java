package io.swkoreatech.kosp.domain.search.dto.response;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

import java.time.LocalDateTime;

public record RepositorySummary(
    String repoOwner,
    String repoName,
    String description,
    String primaryLanguage,
    Integer stargazersCount,
    Integer forksCount,
    LocalDateTime lastCommitDate
) {
    public static RepositorySummary from(GithubRepositoryStatistics stats) {
        return new RepositorySummary(
            stats.getRepoOwner(),
            stats.getRepoName(),
            stats.getDescription(),
            stats.getPrimaryLanguage(),
            stats.getStargazersCount(),
            stats.getForksCount(),
            stats.getLastCommitDate()
        );
    }
}
