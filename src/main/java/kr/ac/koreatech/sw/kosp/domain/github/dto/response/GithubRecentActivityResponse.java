package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;

@Schema(description = "1. 최근 기여활동 항목")
public record GithubRecentActivityResponse(
    String repositoryName,
    String description,
    Integer stargazersCount,
    Integer userCommitCount,
    Integer userPrCount,
    LocalDateTime lastCommitDate
) {
    public static GithubRecentActivityResponse from(GithubRepositoryStatistics repo) {
        return new GithubRecentActivityResponse(
            repo.getRepoName(),
            repo.getDescription(),
            repo.getStargazersCount(),
            repo.getUserCommitsCount(),
            repo.getUserPrsCount(),
            repo.getLastCommitDate()
        );
    }
}
