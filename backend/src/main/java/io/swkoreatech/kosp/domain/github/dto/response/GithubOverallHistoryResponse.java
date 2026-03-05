package io.swkoreatech.kosp.domain.github.dto.response;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GitHub 전체 기여 내역 응답 DTO.
 *
 * @param contributedRepoCount 기여 저장소 수
 * @param totalCommitCount 총 커밋 수
 * @param totalAdditions 총 추가 라인 수
 * @param totalDeletions 총 삭제 라인 수
 * @param totalIssueCount 총 이슈 수
 * @param totalPrCount 총 PR 수
 */
@Schema(description = "2. 전체 기여 내역 항목")
public record GithubOverallHistoryResponse(
    Integer contributedRepoCount,
    Integer totalCommitCount,
    Integer totalAdditions,
    Integer totalDeletions,
    Integer totalIssueCount,
    Integer totalPrCount
) {
    /**
     * GitHub 사용자 통계로부터 전체 기여 내역 응답을 생성한다.
     *
     * @param stats GitHub 사용자 통계
     * @return 전체 기여 내역 응답
     */
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
