package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubYearlyStatistics;
import lombok.Builder;

public record YearlyAnalysisResponse(
    Integer year,
    ScoreInfo score,
    BestInfo best
) {
    @Builder
    public record ScoreInfo(
        BigDecimal totalScore,
        BigDecimal mainRepoScore,
        BigDecimal otherRepoScore,
        BigDecimal prIssueScore,
        BigDecimal reputationScore,
        Integer rank,
        Integer percentile
    ) {}

    @Builder
    public record BestInfo(
        Integer commits,
        Integer commitLines,
        Integer pullRequests,
        Integer issues,
        BestRepository bestRepository
    ) {}

    @Builder
    public record BestRepository(
        String owner,
        String name,
        Integer commits,
        Integer stars
    ) {}

    public static YearlyAnalysisResponse from(
        GithubYearlyStatistics statistics,
        GithubRepositoryStatistics bestRepoStats
    ) {
        ScoreInfo score = ScoreInfo.builder()
            .totalScore(statistics.getTotalScore())
            .mainRepoScore(statistics.getMainRepoScore())
            .otherRepoScore(statistics.getOtherRepoScore())
            .prIssueScore(statistics.getPrIssueScore())
            .reputationScore(statistics.getReputationScore())
            .rank(statistics.getRank())
            .percentile(statistics.getPercentile())
            .build();

        BestRepository bestRepo = null;
        if (statistics.getBestRepoOwner() != null && statistics.getBestRepoName() != null) {
            bestRepo = BestRepository.builder()
                .owner(statistics.getBestRepoOwner())
                .name(statistics.getBestRepoName())
                .commits(statistics.getBestRepoCommits())
                .stars(bestRepoStats != null ? bestRepoStats.getStargazersCount() : 0)
                .build();
        }

        BestInfo best = BestInfo.builder()
            .commits(statistics.getCommits())
            .commitLines(statistics.getLines())
            .pullRequests(statistics.getPrs())
            .issues(statistics.getIssues())
            .bestRepository(bestRepo)
            .build();

        return new YearlyAnalysisResponse(statistics.getYear(), score, best);
    }
}
