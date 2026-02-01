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

    long count();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(g.totalCommits) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    java.math.BigDecimal findAverageCommits();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(g.totalPrs) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    java.math.BigDecimal findAveragePrs();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(g.totalIssues) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    java.math.BigDecimal findAverageIssues();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(g.totalStarsReceived) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    java.math.BigDecimal findAverageStars();

    default GithubUserStatistics getOrCreate(String githubId) {
        return findByGithubId(githubId)
            .orElseGet(() -> save(GithubUserStatistics.create(githubId)));
    }
}
