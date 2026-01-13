package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubContributionPattern;

public interface GithubContributionPatternRepository extends JpaRepository<GithubContributionPattern, Long> {

    Optional<GithubContributionPattern> findByGithubId(String githubId);

    void deleteByGithubId(String githubId);
}
