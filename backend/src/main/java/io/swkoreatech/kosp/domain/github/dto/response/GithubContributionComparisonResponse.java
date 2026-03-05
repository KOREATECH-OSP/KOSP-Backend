package io.swkoreatech.kosp.domain.github.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GitHub 기여 내역 비교 응답 DTO.
 * 전체 평균과 사용자의 기여 통계를 비교한다.
 *
 * @param avgCommitCount 평균 커밋 수
 * @param avgStarCount 평균 스타 수
 * @param avgPrCount 평균 PR 수
 * @param avgIssueCount 평균 이슈 수
 * @param userCommitCount 사용자 커밋 수
 * @param userStarCount 사용자 스타 수
 * @param userPrCount 사용자 PR 수
 * @param userIssueCount 사용자 이슈 수
 */
@Schema(description = "3. 기여내역 비교 항목")
public record GithubContributionComparisonResponse(
    Double avgCommitCount,
    Double avgStarCount,
    Double avgPrCount,
    Double avgIssueCount,

    Integer userCommitCount,
    Integer userStarCount,
    Integer userPrCount,
    Integer userIssueCount
) {
    /**
     * 평균 통계와 사용자 통계로부터 비교 응답을 생성한다.
     *
     * @return 기여 내역 비교 응답
     */
    public static GithubContributionComparisonResponse from(
        Double avgCommitCount,
        Double avgStarCount,
        Double avgPrCount,
        Double avgIssueCount,
        Integer userCommitCount,
        Integer userStarCount,
        Integer userPrCount,
        Integer userIssueCount
    ) {
        return new GithubContributionComparisonResponse(
            avgCommitCount, avgStarCount, avgPrCount, avgIssueCount,
            userCommitCount, userStarCount, userPrCount, userIssueCount
        );
    }

    /**
     * 플랫폼 통계가 없을 때 평균값을 0으로 설정한 비교 응답을 생성한다.
     *
     * @return 빈 평균 통계가 포함된 기여 내역 비교 응답
     */
    public static GithubContributionComparisonResponse empty(
        Integer userCommitCount,
        Integer userStarCount,
        Integer userPrCount,
        Integer userIssueCount
    ) {
        return new GithubContributionComparisonResponse(
            0.0, 0.0, 0.0, 0.0,
            userCommitCount, userStarCount, userPrCount, userIssueCount
        );
    }
}
