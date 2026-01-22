package io.swkoreatech.kosp.domain.mail.repository;

import io.swkoreatech.kosp.domain.mail.model.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
