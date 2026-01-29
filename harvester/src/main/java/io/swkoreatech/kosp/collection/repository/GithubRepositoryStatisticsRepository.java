package io.swkoreatech.kosp.collection.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import io.swkoreatech.kosp.collection.entity.GithubRepositoryStatistics;

public interface GithubRepositoryStatisticsRepository 
    extends CrudRepository<GithubRepositoryStatistics, Long> {
    
    Optional<GithubRepositoryStatistics> findByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );
}
