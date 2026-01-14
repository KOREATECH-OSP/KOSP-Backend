package kr.ac.koreatech.sw.kosp.domain.github.deprecated;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.github.deprecated.GithubLanguageStatistics;

public interface GithubLanguageStatisticsRepository extends JpaRepository<GithubLanguageStatistics, Long> {

    List<GithubLanguageStatistics> findByGithubIdOrderByLinesOfCodeDesc(String githubId);

    Optional<GithubLanguageStatistics> findByGithubIdAndLanguage(String githubId, String language);

    void deleteByGithubId(String githubId);
}
