package io.swkoreatech.kosp.common.github.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;

/**
 * {@link GithubUserStatistics} 엔티티의 데이터 접근 리포지토리.
 *
 * <p>GitHub 사용자 통계의 저장, 조회 및 평균값 집계 기능을 제공한다.</p>
 */
public interface GithubUserStatisticsRepository extends Repository<GithubUserStatistics, Long> {

    /**
     * GitHub 사용자 통계를 저장한다.
     *
     * @param statistics 저장할 통계 엔티티
     * @return 저장된 통계 엔티티
     */
    GithubUserStatistics save(GithubUserStatistics statistics);

    /**
     * GitHub ID로 사용자 통계를 조회한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return 통계 엔티티 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<GithubUserStatistics> findByGithubId(String githubId);

    /**
     * 모든 GitHub 사용자 통계를 조회한다.
     *
     * @return 전체 통계 목록
     */
    List<GithubUserStatistics> findAll();

    /**
     * 총점 내림차순으로 정렬된 모든 GitHub 사용자 통계를 조회한다.
     *
     * @return 총점 순으로 정렬된 통계 목록
     */
    List<GithubUserStatistics> findAllByOrderByTotalScoreDesc();

    /**
     * 총점 내림차순으로 정렬된 GitHub 사용자 통계를 페이징하여 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 총점 순으로 정렬된 통계 페이지
     */
    @Query("SELECT gs FROM GithubUserStatistics gs ORDER BY gs.totalScore DESC")
    Page<GithubUserStatistics> findAllOrderByTotalScoreDesc(Pageable pageable);

    /**
     * 특정 GitHub ID의 통계 존재 여부를 확인한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return 존재하면 {@code true}
     */
    boolean existsByGithubId(String githubId);

    /**
     * 전체 사용자의 커밋, PR, 이슈, 스타 평균과 총 사용자 수를 조회한다.
     *
     * @return 평균 커밋, 평균 PR, 평균 이슈, 평균 스타, 사용자 수가 담긴 배열
     */
    @Query("SELECT " +
        "AVG(u.totalCommits), AVG(u.totalPrs), AVG(u.totalIssues), AVG(u.totalStarsReceived), COUNT(u) " +
        "FROM GithubUserStatistics u")
    Object[] getGlobalAverages();

    /**
     * 전체 통계 레코드 수를 반환한다.
     *
     * @return 통계 레코드 수
     */
    long count();

    /**
     * 특정 점수보다 높은 총점을 가진 통계 레코드 수를 반환한다.
     * 내 랭킹 순위 계산에 사용된다 (rank = count + 1).
     *
     * @param totalScore 기준 점수
     * @return 기준 점수보다 높은 사용자 수
     */
    long countByTotalScoreGreaterThan(BigDecimal totalScore);

    /**
     * 계산 완료된 사용자의 평균 커밋 수를 조회한다.
     *
     * @return 평균 커밋 수
     */
    @Query("SELECT AVG(g.totalCommits) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageCommits();

    /**
     * 계산 완료된 사용자의 평균 PR 수를 조회한다.
     *
     * @return 평균 PR 수
     */
    @Query("SELECT AVG(g.totalPrs) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAveragePrs();

    /**
     * 계산 완료된 사용자의 평균 이슈 수를 조회한다.
     *
     * @return 평균 이슈 수
     */
    @Query("SELECT AVG(g.totalIssues) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageIssues();

    /**
     * 계산 완료된 사용자의 평균 스타 수를 조회한다.
     *
     * @return 평균 스타 수
     */
    @Query("SELECT AVG(g.totalStarsReceived) FROM GithubUserStatistics g WHERE g.calculatedAt IS NOT NULL")
    BigDecimal findAverageStars();

    /**
     * GitHub ID에 해당하는 통계를 조회하거나, 존재하지 않으면 새로 생성하여 반환한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return 기존 또는 새로 생성된 통계 엔티티
     */
    default GithubUserStatistics getOrCreate(String githubId) {
        return findByGithubId(githubId)
            .orElseGet(() -> save(GithubUserStatistics.builder().githubId(githubId).build()));
    }
}
