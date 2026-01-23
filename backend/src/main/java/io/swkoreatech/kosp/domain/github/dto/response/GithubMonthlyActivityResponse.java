package io.swkoreatech.kosp.domain.github.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record GithubMonthlyActivityResponse(
    List<MonthlyActivity> activities
) {
    @Builder
    public record MonthlyActivity(
        Integer year,
        Integer month,
        Integer commitsCount,
        Integer linesCount,
        Integer prsCount,
        Integer issuesCount
    ) {
    }
}
