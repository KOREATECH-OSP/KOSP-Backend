package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

/**
 * 내 GitHub 기여 점수 기반 랭킹 응답 DTO.
 *
 * @param rank           내 순위
 * @param totalScore     총점 (최대 9pt)
 * @param activityScore  활동 수준 점수 (0~3pt)
 * @param diversityScore 다양성 점수 (0~1pt)
 * @param impactScore    영향력 점수 (0~5pt)
 */
public record MyGithubRankingResponse(
    long rank,
    BigDecimal totalScore,
    BigDecimal activityScore,
    BigDecimal diversityScore,
    BigDecimal impactScore
) {
    public static MyGithubRankingResponse of(long rank, GithubUserStatistics stats) {
        return new MyGithubRankingResponse(
            rank,
            stats.getTotalScore(),
            stats.getActivityScore(),
            stats.getDiversityScore(),
            stats.getImpactScore()
        );
    }
}
