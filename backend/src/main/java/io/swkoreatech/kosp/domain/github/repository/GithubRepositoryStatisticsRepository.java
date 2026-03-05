package io.swkoreatech.kosp.domain.github.repository;

import io.swkoreatech.kosp.domain.github.model.GithubRepositoryStatistics;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * GitHub 저장소 통계 리포지토리.
 * 저장소별 기여 통계의 저장 및 조회 기능을 제공한다.
 */
public interface GithubRepositoryStatisticsRepository
        extends Repository<GithubRepositoryStatistics, Long>,
        JpaSpecificationExecutor<GithubRepositoryStatistics> {

    /** 저장소 통계를 저장한다. */
    GithubRepositoryStatistics save(GithubRepositoryStatistics statistics);

    /** 여러 저장소 통계를 일괄 저장한다. */
    List<GithubRepositoryStatistics> saveAll(Iterable<GithubRepositoryStatistics> statistics);

    /** ID로 저장소 통계를 조회한다. */
    Optional<GithubRepositoryStatistics> findById(Long id);

    /** GitHub ID로 저장소 통계 목록을 조회한다. */
    List<GithubRepositoryStatistics> findByContributorGithubId(String githubId);

    /** GitHub ID로 저장소 통계를 마지막 커밋일 기준 내림차순 조회한다. */
    List<GithubRepositoryStatistics> findByContributorGithubIdOrderByLastCommitDateDesc(String githubId);

    /** GitHub ID로 저장소 통계를 스타 수 기준 내림차순 조회한다. */
    List<GithubRepositoryStatistics> findByContributorGithubIdOrderByStargazersCountDesc(String githubId);

    /** 저장소 소유자, 이름, 기여자 GitHub ID로 저장소 통계를 조회한다. */
    Optional<GithubRepositoryStatistics> findByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );

    /** 저장소 소유자, 이름, 기여자 GitHub ID로 저장소 통계 존재 여부를 확인한다. */
    boolean existsByRepoOwnerAndRepoNameAndContributorGithubId(
        String repoOwner,
        String repoName,
        String contributorGithubId
    );

    /** GitHub ID로 최근 커밋일 기준 상위 N개의 저장소 통계를 조회한다. */
    @Query("SELECT r FROM GithubRepositoryStatistics r WHERE r.contributorGithubId = :githubId ORDER BY r.lastCommitDate DESC LIMIT :limit")
    List<GithubRepositoryStatistics> findTopNByContributorGithubIdOrderByLastCommitDateDesc(
        @Param("githubId") String githubId,
        @Param("limit") Integer limit
    );
}
