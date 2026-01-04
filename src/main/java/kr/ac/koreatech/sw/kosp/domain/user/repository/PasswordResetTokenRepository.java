package kr.ac.koreatech.sw.kosp.domain.user.repository;

import kr.ac.koreatech.sw.kosp.domain.user.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
}
