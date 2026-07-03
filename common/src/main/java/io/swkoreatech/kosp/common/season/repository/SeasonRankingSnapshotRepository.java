package io.swkoreatech.kosp.common.season.repository;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingSnapshot;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link SeasonRankingSnapshot} 엔티티 리포지토리.
 */
public interface SeasonRankingSnapshotRepository extends Repository<SeasonRankingSnapshot, Long> {

    SeasonRankingSnapshot save(SeasonRankingSnapshot snapshot);

    /**
     * 특정 시즌·유저의 스냅샷이 이미 존재하는지 확인한다 (시즌 종료 중복 처리 방지).
     */
    boolean existsBySeasonAndUser(Season season, User user);
}
