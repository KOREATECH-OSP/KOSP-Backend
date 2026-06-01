package io.swkoreatech.kosp.common.season.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link SeasonRankingScore} 엔티티 리포지토리.
 */
public interface SeasonRankingScoreRepository extends Repository<SeasonRankingScore, Long> {

    SeasonRankingScore save(SeasonRankingScore score);

    Optional<SeasonRankingScore> findById(Long id);

    Optional<SeasonRankingScore> findBySeasonAndUser(Season season, User user);

    List<SeasonRankingScore> findAllBySeason(Season season);

    Page<SeasonRankingScore> findAllBySeasonOrderByTotalScoreDescRankInSeasonAsc(Season season, Pageable pageable);

    default SeasonRankingScore getBySeasonAndUser(Season season, User user) {
        return findBySeasonAndUser(season, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_RANKING_NOT_FOUND));
    }
}
