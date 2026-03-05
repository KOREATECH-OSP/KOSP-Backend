package io.swkoreatech.kosp.domain.github.dto.response;

import java.time.LocalDate;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import lombok.Builder;

/**
 * 기여 개요 응답 DTO.
 *
 * @param totalRepositories 전체 저장소 수
 * @param ownedRepositories 소유 저장소 수
 * @param contributedRepositories 기여 저장소 수
 * @param totalCommits 총 커밋 수
 * @param totalLines 총 코드 라인 수
 * @param totalAdditions 총 추가 라인 수
 * @param totalDeletions 총 삭제 라인 수
 * @param totalIssues 총 이슈 수
 * @param totalPullRequests 총 PR 수
 * @param totalStarsReceived 총 수신 스타 수
 * @param totalForksReceived 총 수신 포크 수
 * @param dataPeriodStart 데이터 기간 시작일
 * @param dataPeriodEnd 데이터 기간 종료일
 */
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
    /**
     * GitHub 사용자 통계로부터 기여 개요 응답을 생성한다.
     *
     * @param statistics GitHub 사용자 통계
     * @return 기여 개요 응답
     */
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
