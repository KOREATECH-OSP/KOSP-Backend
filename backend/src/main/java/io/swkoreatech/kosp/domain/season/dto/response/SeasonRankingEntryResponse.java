package io.swkoreatech.kosp.domain.season.dto.response;

import java.math.BigDecimal;

import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;

/**
 * 시즌 랭킹 단건 응답 DTO.
 *
 * @param rank       순위 (동점자는 공동 순위)
 * @param userId     유저 ID
 * @param userName   유저 이름
 * @param totalScore 총점
 * @param tier       티어
 */
public record SeasonRankingEntryResponse(
    Integer rank,
    Long userId,
    String userName,
    BigDecimal totalScore,
    SeasonTier tier
) {
    public static SeasonRankingEntryResponse from(SeasonRankingScore score) {
        return new SeasonRankingEntryResponse(
            score.getRankInSeason(),
            score.getUser().getId(),
            score.getUser().getName(),
            score.getTotalScore(),
            score.getTier()
        );
    }
}
