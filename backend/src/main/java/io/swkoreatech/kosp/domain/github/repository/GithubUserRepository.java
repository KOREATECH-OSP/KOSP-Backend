package io.swkoreatech.kosp.domain.github.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.github.model.GithubUser;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * GitHub 사용자 리포지토리.
 * GitHub 사용자의 저장, 조회 기능을 제공한다.
 */
public interface GithubUserRepository extends PagingAndSortingRepository<GithubUser, Long> {

    /** GitHub ID로 사용자를 조회한다. */
    Optional<GithubUser> findByGithubId(Long githubId);

    /** GitHub 로그인명으로 사용자를 조회한다. */
    Optional<GithubUser> findByGithubLogin(String githubLogin);

    /** 모든 GitHub 사용자를 조회한다. */
    List<GithubUser> findAll();

    /** GitHub 사용자를 저장한다. */
    GithubUser save(GithubUser githubUser);

    /** 모든 GitHub ID 목록을 조회한다. */
    @Query("SELECT g.githubId FROM GithubUser g")
    List<Long> findAllGithubIds();

    /**
     * GitHub ID로 사용자를 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param githubId GitHub ID
     * @return GitHub 사용자
     * @throws GlobalException 사용자를 찾을 수 없는 경우
     */
    default GithubUser getByGithubId(Long githubId) {
        return findByGithubId(githubId)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.AUTHENTICATION.getMessage(),
                ExceptionMessage.AUTHENTICATION.getStatus()
            ));
    }

}
