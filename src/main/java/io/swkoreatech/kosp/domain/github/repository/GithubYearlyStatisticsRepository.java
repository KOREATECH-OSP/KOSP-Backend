package io.swkoreatech.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.github.model.GithubYearlyStatistics;

public interface GithubYearlyStatisticsRepository extends Repository<GithubYearlyStatistics, Long> {

    GithubYearlyStatistics save(GithubYearlyStatistics statistics);

    Optional<GithubYearlyStatistics> findByGithubIdAndYear(String githubId, Integer year);

    List<GithubYearlyStatistics> findByGithubIdOrderByYearDesc(String githubId);

    List<GithubYearlyStatistics> findByYearOrderByTotalScoreDesc(Integer year);

    void deleteByGithubId(String githubId);
}
