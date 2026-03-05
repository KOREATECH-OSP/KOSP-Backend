package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.entity.GithubRepositoryStatistics;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * GitHub 저장소 통계 JPA 리포지토리.
 *
 * <p>저장소별 통계 엔티티에 대한 CRUD 및 소유자/이름/기여자별 조회 기능을 제공한다.
 */
public interface GithubRepositoryStatisticsRepository
    extends CrudRepository<GithubRepositoryStatistics, Long> {

    /**
     * 저장소 소유자, 이름, 기여자 GitHub ID로 통계를 조회한다.
     *
     * @param repoOwner          저장소 소유자
     * @param repoName           저장소 이름
     * @param contributorGithubId 기여자 GitHub ID
     * @return 저장소 통계 Optional
     */
    Optional<GithubRepositoryStatistics> findByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );
}
