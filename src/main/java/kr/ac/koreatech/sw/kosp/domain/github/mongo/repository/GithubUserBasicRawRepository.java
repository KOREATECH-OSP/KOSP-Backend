package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserBasicRaw;

public interface GithubUserBasicRawRepository extends Repository<GithubUserBasicRaw, String> {

    GithubUserBasicRaw save(GithubUserBasicRaw raw);

    Optional<GithubUserBasicRaw> findById(String id);

    Optional<GithubUserBasicRaw> findByGithubId(String githubId);

    boolean existsByGithubId(String githubId);
}
