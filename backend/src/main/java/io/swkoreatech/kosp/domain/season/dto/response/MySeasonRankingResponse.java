package io.swkoreatech.kosp.domain.season.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;

/**
 * 내 시즌 랭킹 응답 DTO.
 *
 * @param seasonName       시즌 이름
 * @param endDate          시즌 종료일
 * @param rank             내 순위
 * @param totalScore       총점
 * @param tier             티어
 * @param attendanceScore  출석 점수
 * @param commitScore      커밋 점수
 * @param challengeScore   챌린지 점수
 * @param projectScore     프로젝트 점수
 * @param communityScore   커뮤니티 점수
 */
public record MySeasonRankingResponse(
    String seasonName,
    LocalDate endDate,
    Integer rank,
    BigDecimal totalScore,
    SeasonTier tier,
    BigDecimal attendanceScore,
    BigDecimal commitScore,
    BigDecimal challengeScore,
    BigDecimal projectScore,
    BigDecimal communityScore
) {
    public static MySeasonRankingResponse from(Season season, SeasonRankingScore score) {
        return new MySeasonRankingResponse(
            season.getName(),
            season.getEndDate(),
            score.getRankInSeason(),
            score.getTotalScore(),
            score.getTier(),
            score.getAttendanceScore(),
            score.getCommitScore(),
            score.getChallengeScore(),
            score.getProjectScore(),
            score.getCommunityScore()
        );
    }
}
