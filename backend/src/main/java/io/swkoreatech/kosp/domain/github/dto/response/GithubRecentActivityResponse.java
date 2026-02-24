package io.swkoreatech.kosp.domain.github.dto.response;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "1. 최근 기여활동 항목")
public record GithubRecentActivityResponse(
    String repoOwner,
    String repositoryName,
    String description,
    Integer stargazersCount,
    Integer userCommitCount,
    Integer userPrCount,
    LocalDateTime lastCommitDate
) {
    public static GithubRecentActivityResponse from(GithubRepositoryStatistics repo) {
        return new GithubRecentActivityResponse(
            repo.getRepoOwner(),
            repo.getRepoName(),
            repo.getDescription(),
            repo.getStargazersCount(),
            repo.getUserCommitsCount(),
            repo.getUserPrsCount(),
            repo.getLastCommitDate()
        );
    }
}

