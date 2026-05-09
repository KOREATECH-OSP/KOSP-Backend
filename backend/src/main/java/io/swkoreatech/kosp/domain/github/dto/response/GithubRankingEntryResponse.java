package io.swkoreatech.kosp.domain.github.dto.response;

import java.math.BigDecimal;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * GitHub 기여 점수 기반 랭킹 단건 응답 DTO.
 *
 * @param rank           순위
 * @param userId         유저 ID
 * @param userName       유저 이름
 * @param totalScore     총점 (최대 9pt)
 * @param activityScore  활동 수준 점수 (0~3pt)
 * @param diversityScore 다양성 점수 (0~1pt)
 * @param impactScore    영향력 점수 (0~5pt)
 */
public record GithubRankingEntryResponse(
    Integer rank,
    Long userId,
    String userName,
    BigDecimal totalScore,
    BigDecimal activityScore,
    BigDecimal diversityScore,
    BigDecimal impactScore
) {
    public static GithubRankingEntryResponse of(int rank, User user, GithubUserStatistics stats) {
        return new GithubRankingEntryResponse(
            rank,
            user.getId(),
            user.getName(),
            stats.getTotalScore(),
            stats.getActivityScore(),
            stats.getDiversityScore(),
            stats.getImpactScore()
        );
    }
}
