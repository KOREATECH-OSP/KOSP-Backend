package io.swkoreatech.kosp.statistics.repository;

import io.swkoreatech.kosp.statistics.model.PlatformStatistics;

import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface PlatformStatisticsRepository extends Repository<PlatformStatistics, String> {

    PlatformStatistics save(PlatformStatistics statistics);

    Optional<PlatformStatistics> findByStatKey(String statKey);

    default PlatformStatistics getOrCreate(String statKey) {
        return findByStatKey(statKey)
            .orElseGet(() -> save(PlatformStatistics.create(statKey)));
    }
}
