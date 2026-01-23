package io.swkoreatech.kosp.user;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.github.model.GithubUser;

public interface GithubUserRepository extends Repository<GithubUser, Long> {

    GithubUser save(GithubUser githubUser);

    Optional<GithubUser> findById(Long githubId);

    default GithubUser getById(Long githubId) {
        return findById(githubId).orElseThrow(() -> new RuntimeException("GithubUser not found: " + githubId));
    }

    Optional<GithubUser> findByGithubLogin(String githubLogin);
}
