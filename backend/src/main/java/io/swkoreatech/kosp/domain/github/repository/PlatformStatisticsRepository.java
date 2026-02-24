package io.swkoreatech.kosp.domain.github.repository;

import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface PlatformStatisticsRepository extends Repository<PlatformStatistics, String> {

    Optional<PlatformStatistics> findByStatKey(String statKey);

    default PlatformStatistics getGlobal() {
        return findByStatKey("global").orElse(null);
    }
}
