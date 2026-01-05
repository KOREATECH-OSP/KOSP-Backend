package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;

public interface GithubCommitDetailRawRepository extends Repository<GithubCommitDetailRaw, String> {

    GithubCommitDetailRaw save(GithubCommitDetailRaw raw);

    Optional<GithubCommitDetailRaw> findById(String id);

    Optional<GithubCommitDetailRaw> findBySha(String sha);

    boolean existsBySha(String sha);

    List<GithubCommitDetailRaw> findByAuthorLogin(String login);

    List<GithubCommitDetailRaw> findByRepoOwnerAndRepoName(String repoOwner, String repoName);

    long countByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
