package kr.ac.koreatech.sw.kosp.domain.auth.jwt.repository;

import static kr.ac.koreatech.sw.kosp.global.model.exception.ExceptionMessage.USER_NOT_FOUND;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
import kr.ac.koreatech.sw.kosp.global.model.exception.GlobalException;

public interface JwtTokenRedisRepository extends Repository<JwtToken, Integer> {
    Optional<JwtToken> findById(Integer userId);

    default JwtToken getById(Integer userId) {
        return findById(userId).orElseThrow(
            () -> new GlobalException(USER_NOT_FOUND.getMessage(), USER_NOT_FOUND.getStatus())
        );
    }

    JwtToken save(JwtToken updatedStatus);

    void deleteById(Integer id);
}
