package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GithubProfileRepository extends Repository<GithubProfile, Long> {
    GithubProfile save(GithubProfile profile);
    Optional<GithubProfile> findByGithubId(Long githubId);
}
