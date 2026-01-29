package io.swkoreatech.kosp.statistics.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

public interface GithubUserStatisticsRepository extends Repository<GithubUserStatistics, Long> {

    GithubUserStatistics save(GithubUserStatistics githubUserStatistics);

    Optional<GithubUserStatistics> findByGithubId(String githubId);

    List<GithubUserStatistics> findAll();

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
            .orElseGet(() -> save(GithubUserStatistics.create(githubId)));
    }
}
