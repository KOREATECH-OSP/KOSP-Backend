package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubYearlyStatistics;

public interface GithubYearlyStatisticsRepository extends JpaRepository<GithubYearlyStatistics, Long> {

    Optional<GithubYearlyStatistics> findByGithubIdAndYear(String githubId, Integer year);

    List<GithubYearlyStatistics> findByGithubIdOrderByYearDesc(String githubId);

    List<GithubYearlyStatistics> findByYearOrderByTotalScoreDesc(Integer year);

    void deleteByGithubId(String githubId);
}
