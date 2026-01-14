package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;

@Schema(description = "2. 전체 기여 내역 항목")
public record GithubOverallHistoryResponse(
    Integer contributedRepoCount,
    Integer totalCommitCount,
    Integer totalAdditions,
    Integer totalDeletions,
    Integer totalIssueCount,
    Integer totalPrCount
) {
    public static GithubOverallHistoryResponse from(GithubUserStatistics stats) {
        return new GithubOverallHistoryResponse(
            stats.getContributedReposCount(),
            stats.getTotalCommits(),
            stats.getTotalAdditions(),
            stats.getTotalDeletions(),
            stats.getTotalIssues(),
            stats.getTotalPrs()
        );
    }
}
