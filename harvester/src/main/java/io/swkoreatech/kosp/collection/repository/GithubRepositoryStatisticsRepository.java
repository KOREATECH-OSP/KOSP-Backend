package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.entity.GithubRepositoryStatistics;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface GithubRepositoryStatisticsRepository 
    extends CrudRepository<GithubRepositoryStatistics, Long> {
    
    Optional<GithubRepositoryStatistics> findByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );
}
