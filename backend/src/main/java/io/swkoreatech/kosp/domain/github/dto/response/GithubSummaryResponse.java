package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record GithubSummaryResponse(
    String githubId,
    Integer totalCommits,
    Integer totalLines,
    Integer totalAdditions,
    Integer totalDeletions,
    Integer totalPrs,
    Integer totalIssues,
    Integer ownedReposCount,
    Integer contributedReposCount,
    Integer totalStarsReceived,
    BigDecimal totalScore,
    LocalDateTime calculatedAt,
    LocalDate dataPeriodStart,
    LocalDate dataPeriodEnd
) {
}
