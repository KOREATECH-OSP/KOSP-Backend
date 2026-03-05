package io.swkoreatech.kosp.domain.github.dto.response;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;
import io.swkoreatech.kosp.domain.github.model.GithubYearlyStatistics;

import java.math.BigDecimal;

import lombok.Builder;

/**
 * 연도별 분석 응답 DTO.
 *
 * @param year 연도
 * @param score 점수 정보
 * @param best 최고 기여 정보
 */
public record YearlyAnalysisResponse(
    Integer year,
    ScoreInfo score,
    BestInfo best
) {
    /**
     * 점수 상세 정보.
     *
     * @param totalScore 총점
     * @param mainRepoScore 메인 저장소 점수
     * @param otherRepoScore 기타 저장소 점수
     * @param prIssueScore PR/이슈 점수
     * @param reputationScore 평판 점수
     * @param rank 순위
     * @param percentile 백분위
     */
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

    /**
     * 최고 기여 정보.
     *
     * @param commits 커밋 수
     * @param commitLines 커밋 라인 수
     * @param pullRequests PR 수
     * @param issues 이슈 수
     * @param bestRepository 최고 저장소 정보
     */
    @Builder
    public record BestInfo(
        Integer commits,
        Integer commitLines,
        Integer pullRequests,
        Integer issues,
        BestRepository bestRepository
    ) {}

    /**
     * 최고 저장소 정보.
     *
     * @param owner 소유자
     * @param name 저장소 이름
     * @param commits 커밋 수
     * @param stars 스타 수
     */
    @Builder
    public record BestRepository(
        String owner,
        String name,
        Integer commits,
        Integer stars
    ) {}

    /**
     * 연도별 통계와 최고 저장소 통계로부터 분석 응답을 생성한다.
     *
     * @param statistics 연도별 통계
     * @param bestRepoStats 최고 저장소 통계
     * @return 연도별 분석 응답
     */
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
