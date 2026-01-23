package io.swkoreatech.kosp.harvester.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubUserRepository extends JpaRepository<GithubUser, Long> {

    Optional<GithubUser> findById(Long githubId);

    default GithubUser getById(Long githubId) {
        return findById(githubId).orElseThrow(() -> new RuntimeException("GithubUser not found: " + githubId));
    }

    Optional<GithubUser> findByGithubLogin(String githubLogin);
}
