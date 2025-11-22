package kr.ac.koreatech.sw.kosp.domain.user.repository;

import static kr.ac.koreatech.sw.kosp.global.model.exception.ExceptionMessage.USER_NOT_FOUND;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.model.exception.GlobalException;

public interface UserRepository extends Repository<User, Integer> {

    User save(User user);

    Optional<User> findById(Integer id);

    Optional<User> findByUnivEmail(String univEmail);

    default User getById(Integer id) {
        return findById(id).orElseThrow(
            () -> new GlobalException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getStatus())
        );
    }

    default User getByUnivEmail(String email) {
        return findByUnivEmail(email).orElseThrow(
            () -> new GlobalException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getStatus())
        );
    }

    void deleteById(Integer id);

}
