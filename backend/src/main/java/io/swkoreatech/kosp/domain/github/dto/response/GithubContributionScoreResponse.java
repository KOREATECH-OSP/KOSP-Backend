package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

/**
 * GitHub 기여 점수 응답 DTO.
 *
 * @param activityScore 활동 수준 점수
 * @param diversityScore 다양성 점수
 * @param impactScore 영향력 점수
 * @param totalScore 총점
 */
@Schema(description = "4. GitHub 기여점수 항목")
public record GithubContributionScoreResponse(
    @Schema(description = "활동 수준 점수 (0~3)")
    BigDecimal activityScore,
    @Schema(description = "다양성 점수 (0~1)")
    BigDecimal diversityScore,
    @Schema(description = "영향력 점수 (0~5)")
    BigDecimal impactScore,
    @Schema(description = "총점")
    BigDecimal totalScore
) {
    /**
     * GitHub 사용자 통계로부터 기여 점수 응답을 생성한다.
     *
     * @param stats GitHub 사용자 통계
     * @return 기여 점수 응답
     */
    public static GithubContributionScoreResponse from(GithubUserStatistics stats) {
        return new GithubContributionScoreResponse(
            stats.getActivityScore(),
            stats.getDiversityScore(),
            stats.getImpactScore(),
            stats.getTotalScore()
        );
    }
}
