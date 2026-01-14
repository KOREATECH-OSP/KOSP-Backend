package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubGlobalStatistics;

public interface GithubGlobalStatisticsRepository extends JpaRepository<GithubGlobalStatistics, Long> {

    @Query("SELECT s FROM GithubGlobalStatistics s ORDER BY s.calculatedAt DESC LIMIT 1")
    Optional<GithubGlobalStatistics> findTopByOrderByCalculatedAtDesc();
}
