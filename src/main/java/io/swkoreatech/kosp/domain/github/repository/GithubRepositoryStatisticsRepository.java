package io.swkoreatech.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

public interface GithubRepositoryStatisticsRepository extends Repository<GithubRepositoryStatistics, Long> {

    GithubRepositoryStatistics save(GithubRepositoryStatistics statistics);

    List<GithubRepositoryStatistics> saveAll(Iterable<GithubRepositoryStatistics> statistics);

    Optional<GithubRepositoryStatistics> findById(Long id);

    List<GithubRepositoryStatistics> findByContributorGithubId(String githubId);

    List<GithubRepositoryStatistics> findByContributorGithubIdOrderByLastCommitDateDesc(String githubId);

    List<GithubRepositoryStatistics> findByContributorGithubIdOrderByStargazersCountDesc(String githubId);

    Optional<GithubRepositoryStatistics> findByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );

    boolean existsByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );

    @Query("SELECT r FROM GithubRepositoryStatistics r WHERE r.contributorGithubId = :githubId ORDER BY r.lastCommitDate DESC LIMIT :limit")
    List<GithubRepositoryStatistics> findTopNByContributorGithubIdOrderByLastCommitDateDesc(
        @Param("githubId") String githubId,
        @Param("limit") Integer limit
    );
}
