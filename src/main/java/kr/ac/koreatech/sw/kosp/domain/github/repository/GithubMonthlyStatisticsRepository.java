package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;

public interface GithubMonthlyStatisticsRepository extends JpaRepository<GithubMonthlyStatistics, Long> {

    GithubMonthlyStatistics save(GithubMonthlyStatistics statistics);

    // This method is typically inherited from JpaRepository, but explicitly defined here.
    // If JpaRepository is used, saveAll is already available.
    // List<GithubMonthlyStatistics> saveAll(Iterable<GithubMonthlyStatistics> statistics);

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId AND m.year = :year AND m.month = :month")
    Optional<GithubMonthlyStatistics> findByGithubIdAndYearAndMonth(
        @Param("githubId") String githubId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId")
    List<GithubMonthlyStatistics> findByGithubId(@Param("githubId") String githubId);

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId ORDER BY m.year DESC")
    List<GithubMonthlyStatistics> findByGithubIdOrderByYearDesc(@Param("githubId") String githubId);

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId ORDER BY m.year DESC, m.month DESC")
    List<GithubMonthlyStatistics> findByGithubIdOrderByYearDescMonthDesc(@Param("githubId") String githubId);

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId AND m.year = :year")
    List<GithubMonthlyStatistics> findByGithubIdAndYear(
        @Param("githubId") String githubId,
        @Param("year") Integer year
    );

    @Query("SELECT m FROM GithubMonthlyStatistics m WHERE m.githubId = :githubId " +
           "AND ((m.year > :startYear) OR (m.year = :startYear AND m.month >= :startMonth)) " +
           "AND ((m.year < :endYear) OR (m.year = :endYear AND m.month <= :endMonth)) " +
           "ORDER BY m.year, m.month")
    List<GithubMonthlyStatistics> findByGithubIdAndYearMonthBetween(
        @Param("githubId") String githubId,
        @Param("startYear") Integer startYear,
        @Param("startMonth") Integer startMonth,
        @Param("endYear") Integer endYear,
        @Param("endMonth") Integer endMonth
    );
}
