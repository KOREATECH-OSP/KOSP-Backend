package io.swkoreatech.kosp.common.github.repository;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface GithubUserStatisticsRepository extends Repository<GithubUserStatistics, Long> {

    GithubUserStatistics save(GithubUserStatistics statistics);

    Optional<GithubUserStatistics> findByGithubId(String githubId);

    List<GithubUserStatistics> findAll();

    List<GithubUserStatistics> findAllByOrderByTotalScoreDesc();

    boolean existsByGithubId(String githubId);

    @Query("SELECT " +
        "AVG(u.totalCommits), AVG(u.totalPrs), AVG(u.totalIssues), AVG(u.totalStarsReceived), COUNT(u) " +
        "FROM GithubUserStatistics u")
    Object[] getGlobalAverages();

    long count();

    @Query("SELECT AVG(g.totalCommits) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageCommits();

    @Query("SELECT AVG(g.totalPrs) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAveragePrs();

    @Query("SELECT AVG(g.totalIssues) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageIssues();

    @Query("SELECT AVG(g.totalStarsReceived) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageStars();

    default GithubUserStatistics getOrCreate(String githubId) {
        return findByGithubId(githubId)
            .orElseGet(() -> save(GithubUserStatistics.builder().githubId(githubId).build()));
    }
}
