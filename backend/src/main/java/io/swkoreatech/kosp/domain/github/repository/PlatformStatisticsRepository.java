package io.swkoreatech.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;

public interface PlatformStatisticsRepository extends Repository<PlatformStatistics, String> {

    Optional<PlatformStatistics> findByStatKey(String statKey);

    default PlatformStatistics getGlobal() {
        return findByStatKey("global").orElse(null);
    }
}
