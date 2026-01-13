package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.time.LocalDate;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import lombok.Builder;

@Builder
public record ContributionOverviewResponse(
    Integer totalRepositories,
    Integer ownedRepositories,
    Integer contributedRepositories,
    Integer totalCommits,
    Integer totalLines,
    Integer totalAdditions,
    Integer totalDeletions,
    Integer totalIssues,
    Integer totalPullRequests,
    Integer totalStarsReceived,
    Integer totalForksReceived,
    LocalDate dataPeriodStart,
    LocalDate dataPeriodEnd
) {
    public static ContributionOverviewResponse from(GithubUserStatistics statistics) {
        return ContributionOverviewResponse.builder()
            .totalRepositories(statistics.getOwnedReposCount() + statistics.getContributedReposCount())
            .ownedRepositories(statistics.getOwnedReposCount())
            .contributedRepositories(statistics.getContributedReposCount())
            .totalCommits(statistics.getTotalCommits())
            .totalLines(statistics.getTotalLines())
            .totalAdditions(statistics.getTotalAdditions())
            .totalDeletions(statistics.getTotalDeletions())
            .totalIssues(statistics.getTotalIssues())
            .totalPullRequests(statistics.getTotalPrs())
            .totalStarsReceived(statistics.getTotalStarsReceived())
            .totalForksReceived(statistics.getTotalForksReceived())
            .dataPeriodStart(statistics.getDataPeriodStart())
            .dataPeriodEnd(statistics.getDataPeriodEnd())
            .build();
    }
}
