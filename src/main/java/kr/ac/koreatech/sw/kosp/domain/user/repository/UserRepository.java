package kr.ac.koreatech.sw.kosp.domain.user.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findById(Integer id);

    Optional<User> findByKutEmail(String kutEmail);

    Optional<User> findByGithubUser_GithubId(Long githubId);

    void deleteById(Integer id);

    default User getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    default User getByKutEmail(String kutEmail) {
        return findByKutEmail(kutEmail)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
