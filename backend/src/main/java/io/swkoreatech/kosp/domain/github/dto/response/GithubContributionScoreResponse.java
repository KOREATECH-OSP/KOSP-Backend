package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

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
    public static GithubContributionScoreResponse from(GithubUserStatistics stats) {
        return new GithubContributionScoreResponse(
            stats.getActivityScore(),
            stats.getDiversityScore(),
            stats.getImpactScore(),
            stats.getTotalScore()
        );
    }
}
