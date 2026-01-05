package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;

public interface GithubMonthlyStatisticsRepository extends Repository<GithubMonthlyStatistics, Long> {

    GithubMonthlyStatistics save(GithubMonthlyStatistics statistics);

    List<GithubMonthlyStatistics> saveAll(Iterable<GithubMonthlyStatistics> statistics);

    Optional<GithubMonthlyStatistics> findByGithubIdAndYearAndMonth(String githubId, Integer year, Integer month);

    List<GithubMonthlyStatistics> findByGithubId(String githubId);

    List<GithubMonthlyStatistics> findByGithubIdOrderByYearDesc(String githubId);

    List<GithubMonthlyStatistics> findByGithubIdOrderByYearDescMonthDesc(String githubId);

    List<GithubMonthlyStatistics> findByGithubIdAndYear(String githubId, Integer year);
}
