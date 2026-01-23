package io.swkoreatech.kosp.statistics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.statistics.model.PlatformStatistics;

public interface PlatformStatisticsRepository extends JpaRepository<PlatformStatistics, String> {

    Optional<PlatformStatistics> findByStatKey(String statKey);

    default PlatformStatistics getOrCreate(String statKey) {
        return findByStatKey(statKey)
            .orElseGet(() -> save(PlatformStatistics.create(statKey)));
    }
}
