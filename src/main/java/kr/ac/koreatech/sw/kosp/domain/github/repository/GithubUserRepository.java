package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;

public interface GithubUserRepository extends Repository<GithubUser, Long> {

    Optional<GithubUser> findByGithubId(Long githubId);

    GithubUser save(GithubUser githubUser);

}
