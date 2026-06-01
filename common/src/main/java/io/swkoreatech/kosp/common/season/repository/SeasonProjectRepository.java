package io.swkoreatech.kosp.common.season.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonProject;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectStatus;

/**
 * {@link SeasonProject} 엔티티 리포지토리.
 */
public interface SeasonProjectRepository extends Repository<SeasonProject, Long> {

    SeasonProject save(SeasonProject project);

    Optional<SeasonProject> findById(Long id);

    List<SeasonProject> findAllBySeasonAndStatus(Season season, SeasonProjectStatus status);

    Page<SeasonProject> findAllBySeasonOrderByCreatedAtDesc(Season season, Pageable pageable);

    default SeasonProject getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_PROJECT_NOT_FOUND));
    }
}
