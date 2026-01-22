package io.swkoreatech.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import io.swkoreatech.kosp.domain.github.model.GithubUser;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

public interface GithubUserRepository extends PagingAndSortingRepository<GithubUser, Long> {

    Optional<GithubUser> findByGithubId(Long githubId);
    
    Optional<GithubUser> findByGithubLogin(String githubLogin);
    
    List<GithubUser> findAll();

    GithubUser save(GithubUser githubUser);

    @Query("SELECT g.githubId FROM GithubUser g")
    List<Long> findAllGithubIds();

    default GithubUser getByGithubId(Long githubId) {
        return findByGithubId(githubId)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.AUTHENTICATION.getMessage(),
                ExceptionMessage.AUTHENTICATION.getStatus()
            ));
    }

}
