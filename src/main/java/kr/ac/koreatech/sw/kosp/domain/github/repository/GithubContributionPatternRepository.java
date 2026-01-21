package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubContributionPattern;

public interface GithubContributionPatternRepository extends Repository<GithubContributionPattern, Long> {

    GithubContributionPattern save(GithubContributionPattern pattern);

    Optional<GithubContributionPattern> findByGithubId(String githubId);

    void deleteByGithubId(String githubId);
}
