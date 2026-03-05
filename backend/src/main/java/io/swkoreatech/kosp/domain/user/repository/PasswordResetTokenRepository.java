package io.swkoreatech.kosp.domain.user.repository;

import org.springframework.data.repository.CrudRepository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.user.model.PasswordResetToken;

/**
 * 비밀번호 재설정 토큰 리포지토리.
 * Redis 기반으로 비밀번호 재설정 토큰의 CRUD 기능을 제공한다.
 */
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {

    /**
     * ID로 비밀번호 재설정 토큰을 조회한다.
     *
     * @param id 토큰 ID
     * @return 비밀번호 재설정 토큰
     * @throws GlobalException 토큰이 존재하지 않는 경우
     */
    default PasswordResetToken getById(String id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.INVALID_VERIFICATION_CODE));
    }
}
