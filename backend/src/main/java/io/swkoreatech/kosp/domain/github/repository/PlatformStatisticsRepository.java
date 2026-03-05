package io.swkoreatech.kosp.domain.github.repository;

import io.swkoreatech.kosp.domain.github.model.PlatformStatistics;

import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 플랫폼 통계 리포지토리.
 * 전체 사용자 평균 통계의 조회 기능을 제공한다.
 */
public interface PlatformStatisticsRepository extends Repository<PlatformStatistics, String> {

    /** 통계 키로 플랫폼 통계를 조회한다. */
    Optional<PlatformStatistics> findByStatKey(String statKey);

    /**
     * 전체 플랫폼 통계를 조회한다.
     *
     * @return 전체 통계, 없으면 null
     */
    default PlatformStatistics getGlobal() {
        return findByStatKey("global").orElse(null);
    }
}
