package io.swkoreatech.kosp.domain.user.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.user.model.PasswordResetToken;

import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {

    default PasswordResetToken getById(String id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.INVALID_VERIFICATION_CODE));
    }
}
