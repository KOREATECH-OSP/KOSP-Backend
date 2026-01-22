package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record GithubRecentContributionsResponse(
    List<RecentRepository> repositories
) {
    @Builder
    public record RecentRepository(
        String repoOwner,
        String repoName,
        Integer stargazersCount,
        Integer userCommitsCount,
        Integer userPrsCount,
        Integer userIssuesCount,
        LocalDateTime lastCommitDate,
        String primaryLanguage
    ) {
    }
}
