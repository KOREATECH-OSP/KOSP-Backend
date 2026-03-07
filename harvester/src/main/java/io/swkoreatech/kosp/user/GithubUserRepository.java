package io.swkoreatech.kosp.user;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.github.model.GithubUser;

/**
 * GitHub 사용자 JPA 리포지토리.
 *
 * <p>GitHub 사용자 엔티티에 대한 저장, 조회 기능을 제공한다.
 */
public interface GithubUserRepository extends Repository<GithubUser, Long> {

    /**
     * GitHub 사용자를 저장한다.
     *
     * @param githubUser 저장할 GitHub 사용자 엔티티
     * @return 저장된 GitHub 사용자 엔티티
     */
    GithubUser save(GithubUser githubUser);

    /**
     * GitHub ID로 사용자를 조회한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return GitHub 사용자 Optional
     */
    Optional<GithubUser> findById(Long githubId);

    /**
     * GitHub ID로 사용자를 조회하되, 없으면 예외를 발생시킨다.
     *
     * @param githubId GitHub 사용자 ID
     * @return GitHub 사용자 엔티티
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    default GithubUser getById(Long githubId) {
        return findById(githubId).orElseThrow(() -> new RuntimeException("GithubUser not found: " + githubId));
    }

    /**
     * GitHub 로그인 이름으로 사용자를 조회한다.
     *
     * @param githubLogin GitHub 로그인 이름
     * @return GitHub 사용자 Optional
     */
    Optional<GithubUser> findByGithubLogin(String githubLogin);
}
