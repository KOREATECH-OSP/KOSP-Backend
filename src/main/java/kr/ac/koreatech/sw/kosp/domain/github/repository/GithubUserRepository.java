package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

public interface GithubUserRepository extends PagingAndSortingRepository<GithubUser, Long> {

    Optional<GithubUser> findByGithubId(Long githubId);
    
    Optional<GithubUser> findByGithubLogin(String githubLogin);
    
    List<GithubUser> findAll();

    GithubUser save(GithubUser githubUser);

    default GithubUser getByGithubId(Long githubId) {
        return findByGithubId(githubId)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.AUTHENTICATION.getMessage(),
                ExceptionMessage.AUTHENTICATION.getStatus()
            ));
    }

}
