package io.swkoreatech.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

public interface GithubUserStatisticsRepository extends Repository<GithubUserStatistics, Long> {

    GithubUserStatistics save(GithubUserStatistics statistics);

    Optional<GithubUserStatistics> findByGithubId(String githubId);

    List<GithubUserStatistics> findAll();

    List<GithubUserStatistics> findAllByOrderByTotalScoreDesc();

    boolean existsByGithubId(String githubId);

    @org.springframework.data.jpa.repository.Query("SELECT " +
        "AVG(u.totalCommits), AVG(u.totalPrs), AVG(u.totalIssues), AVG(u.totalStarsReceived), COUNT(u) " +
        "FROM GithubUserStatistics u")
    Object[] getGlobalAverages();
}
