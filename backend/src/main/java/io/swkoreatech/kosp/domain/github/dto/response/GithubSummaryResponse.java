package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;

/**
 * GitHub 요약 통계 응답 DTO.
 *
 * @param githubId GitHub ID
 * @param totalCommits 총 커밋 수
 * @param totalLines 총 코드 라인 수
 * @param totalAdditions 총 추가 라인 수
 * @param totalDeletions 총 삭제 라인 수
 * @param totalPrs 총 PR 수
 * @param totalIssues 총 이슈 수
 * @param ownedReposCount 소유 저장소 수
 * @param contributedReposCount 기여 저장소 수
 * @param totalStarsReceived 총 수신 스타 수
 * @param totalScore 총 점수
 * @param calculatedAt 산출 일시
 * @param dataPeriodStart 데이터 기간 시작일
 * @param dataPeriodEnd 데이터 기간 종료일
 */
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
