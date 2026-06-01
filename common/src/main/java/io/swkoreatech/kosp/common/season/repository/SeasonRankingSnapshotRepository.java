package io.swkoreatech.kosp.common.season.repository;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.season.model.SeasonRankingSnapshot;

/**
 * {@link SeasonRankingSnapshot} 엔티티 리포지토리.
 */
public interface SeasonRankingSnapshotRepository extends Repository<SeasonRankingSnapshot, Long> {

    SeasonRankingSnapshot save(SeasonRankingSnapshot snapshot);
}
