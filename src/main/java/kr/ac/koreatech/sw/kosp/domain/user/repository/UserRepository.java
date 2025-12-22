package kr.ac.koreatech.sw.kosp.domain.user.repository;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.USER_NOT_FOUND;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findById(Integer id);

    Optional<User> findByKutEmail(String univEmail);

    Optional<User> findByGithubUser_GithubId(Long githubId);

    default User getById(Integer id) {
        return findById(id).orElseThrow(
            () -> new GlobalException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getStatus())
        );
    }

    default User getByKutEmail(String email) {
        return findByKutEmail(email).orElseThrow(
            () -> new GlobalException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getStatus())
        );
    }

    void deleteById(Integer id);

}
