package io.swkoreatech.kosp.domain.github.repository;

import io.swkoreatech.kosp.domain.github.model.GithubYearlyStatistics;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * GitHub 연도별 통계 리포지토리.
 * 연도별 기여 통계의 저장, 조회, 삭제 기능을 제공한다.
 */
public interface GithubYearlyStatisticsRepository extends Repository<GithubYearlyStatistics, Long> {

    /** 연도별 통계를 저장한다. */
    GithubYearlyStatistics save(GithubYearlyStatistics statistics);

    /** GitHub ID와 연도로 통계를 조회한다. */
    Optional<GithubYearlyStatistics> findByGithubIdAndYear(String githubId, Integer year);

    /** GitHub ID로 연도별 통계를 연도 내림차순 조회한다. */
    List<GithubYearlyStatistics> findByGithubIdOrderByYearDesc(String githubId);

    /** 특정 연도의 통계를 총점 기준 내림차순 조회한다. */
    List<GithubYearlyStatistics> findByYearOrderByTotalScoreDesc(Integer year);

    /** GitHub ID에 해당하는 모든 연도별 통계를 삭제한다. */
    void deleteByGithubId(String githubId);
}
