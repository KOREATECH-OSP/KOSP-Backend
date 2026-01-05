package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;

public interface GithubUserStatisticsRepository extends Repository<GithubUserStatistics, Long> {

    GithubUserStatistics save(GithubUserStatistics statistics);

    Optional<GithubUserStatistics> findByGithubId(String githubId);

    List<GithubUserStatistics> findAll();

    List<GithubUserStatistics> findAllByOrderByTotalScoreDesc();

    boolean existsByGithubId(String githubId);
}
