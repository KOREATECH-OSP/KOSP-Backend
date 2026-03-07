package io.swkoreatech.kosp.statistics.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.statistics.model.PlatformStatistics;

/**
 * 플랫폼 통계 JPA 리포지토리.
 *
 * <p>플랫폼 전체 통계 엔티티에 대한 저장 및 조회 기능을 제공한다.
 */
public interface PlatformStatisticsRepository extends Repository<PlatformStatistics, String> {

    /**
     * 플랫폼 통계를 저장한다.
     *
     * @param statistics 저장할 통계 엔티티
     * @return 저장된 통계 엔티티
     */
    PlatformStatistics save(PlatformStatistics statistics);

    /**
     * 통계 키로 플랫폼 통계를 조회한다.
     *
     * @param statKey 통계 식별 키
     * @return 플랫폼 통계 Optional
     */
    Optional<PlatformStatistics> findByStatKey(String statKey);

    /**
     * 통계 키로 플랫폼 통계를 조회하되, 없으면 새로 생성하여 반환한다.
     *
     * @param statKey 통계 식별 키
     * @return 플랫폼 통계 인스턴스
     */
    default PlatformStatistics getOrCreate(String statKey) {
        return findByStatKey(statKey)
            .orElseGet(() -> save(PlatformStatistics.create(statKey)));
    }
}
