package io.swkoreatech.kosp.domain.mail.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.mail.model.EmailVerification;

import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {

    default EmailVerification getById(String id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.EMAIL_NOT_FOUND));
    }
}
