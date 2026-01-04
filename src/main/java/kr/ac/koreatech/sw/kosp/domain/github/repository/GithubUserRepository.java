package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

public interface GithubUserRepository extends Repository<GithubUser, Long> {

    Optional<GithubUser> findByGithubId(Long githubId);

    GithubUser save(GithubUser githubUser);

    default GithubUser getByGithubId(Long githubId) {
        return findByGithubId(githubId)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.AUTHENTICATION.getMessage(),
                ExceptionMessage.AUTHENTICATION.getStatus()
            ));
    }

}
