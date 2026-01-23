package io.swkoreatech.kosp.domain.user.repository;

import io.swkoreatech.kosp.domain.user.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}
