package io.swkoreatech.kosp.common.season.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;

/**
 * {@link Season} 엔티티 리포지토리.
 */
public interface SeasonRepository extends Repository<Season, Long> {

    Season save(Season season);

    Optional<Season> findById(Long id);

    Optional<Season> findByIsActiveTrue();

    default Season getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_NOT_FOUND));
    }

    default Season getActiveSeason() {
        return findByIsActiveTrue().orElseThrow(() -> new GlobalException(ExceptionMessage.ACTIVE_SEASON_NOT_FOUND));
    }
}
