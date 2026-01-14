package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;

@Schema(description = "4. GitHub 기여점수 항목")
public record GithubContributionScoreResponse(
    BigDecimal activityScore, // 4.1
    BigDecimal diversityScore, // 4.2
    BigDecimal impactScore, // 4.3
    BigDecimal totalScore
) {
    public static GithubContributionScoreResponse from(GithubUserStatistics stats) {
        return new GithubContributionScoreResponse(
            stats.getMainRepoScore(),
            stats.getOtherRepoScore(),
            stats.getPrIssueScore().add(stats.getReputationScore()),
            stats.getTotalScore()
        );
    }
}
