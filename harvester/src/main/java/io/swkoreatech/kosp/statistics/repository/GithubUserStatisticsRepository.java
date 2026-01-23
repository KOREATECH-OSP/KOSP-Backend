package io.swkoreatech.kosp.statistics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

public interface GithubUserStatisticsRepository extends JpaRepository<GithubUserStatistics, Long> {

    Optional<GithubUserStatistics> findByGithubId(String githubId);

    default GithubUserStatistics getOrCreate(String githubId) {
        return findByGithubId(githubId)
            .orElseGet(() -> save(GithubUserStatistics.create(githubId)));
    }
}
