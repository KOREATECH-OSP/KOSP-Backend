package io.swkoreatech.kosp.domain.season.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.season.dto.response.MySeasonRankingResponse;
import io.swkoreatech.kosp.domain.season.dto.response.SeasonRankingListResponse;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 랭킹 조회 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonRankingService {

    private final SeasonRepository seasonRepository;
    private final SeasonRankingScoreRepository rankingScoreRepository;

    /**
     * 현재 활성 시즌의 전체 랭킹을 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 랭킹 목록
     */
    public SeasonRankingListResponse getRankings(Pageable pageable) {
        Season season = seasonRepository.getActiveSeason();
        Page<SeasonRankingScore> page = rankingScoreRepository
            .findAllBySeasonOrderByTotalScoreDescRankInSeasonAsc(season, pageable);
        return SeasonRankingListResponse.of(season, page);
    }

    /**
     * 특정 시즌의 전체 랭킹을 조회한다.
     *
     * @param seasonId 시즌 ID
     * @param pageable 페이징 정보
     * @return 랭킹 목록
     */
    public SeasonRankingListResponse getRankingsBySeason(Long seasonId, Pageable pageable) {
        Season season = seasonRepository.getById(seasonId);
        Page<SeasonRankingScore> page = rankingScoreRepository
            .findAllBySeasonOrderByTotalScoreDescRankInSeasonAsc(season, pageable);
        return SeasonRankingListResponse.of(season, page);
    }

    /**
     * 현재 활성 시즌에서 로그인 유저의 랭킹을 조회한다.
     *
     * @param user 로그인 유저
     * @return 내 랭킹 정보
     */
    public MySeasonRankingResponse getMyRanking(User user) {
        Season season = seasonRepository.getActiveSeason();
        SeasonRankingScore score = rankingScoreRepository.findBySeasonAndUser(season, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_RANKING_NOT_FOUND));
        return MySeasonRankingResponse.from(season, score);
    }

    /**
     * 특정 시즌에서 로그인 유저의 랭킹을 조회한다.
     */
    public MySeasonRankingResponse getMyRankingBySeason(Long seasonId, User user) {
        Season season = seasonRepository.getById(seasonId);
        SeasonRankingScore score = rankingScoreRepository.getBySeasonAndUser(season, user);
        return MySeasonRankingResponse.from(season, score);
    }
}
