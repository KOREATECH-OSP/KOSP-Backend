package io.swkoreatech.kosp.domain.github.dto.response;

import java.util.List;

import lombok.Builder;

/**
 * GitHub 월별 활동 응답 DTO.
 *
 * @param activities 월별 활동 목록
 */
@Builder
public record GithubMonthlyActivityResponse(
    List<MonthlyActivity> activities
) {
    /**
     * 월별 활동 상세 정보.
     *
     * @param year 연도
     * @param month 월
     * @param commitsCount 커밋 수
     * @param linesCount 코드 라인 수
     * @param prsCount PR 수
     * @param issuesCount 이슈 수
     */
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
