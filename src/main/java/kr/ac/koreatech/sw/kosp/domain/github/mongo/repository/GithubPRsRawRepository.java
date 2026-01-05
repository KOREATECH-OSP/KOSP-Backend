package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRsRaw;

public interface GithubPRsRawRepository extends Repository<GithubPRsRaw, String> {

    GithubPRsRaw save(GithubPRsRaw raw);

    Optional<GithubPRsRaw> findByRepoOwnerAndRepoName(String repoOwner, String repoName);

    boolean existsByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
