package kr.ac.koreatech.sw.kosp.domain.mail.repository;

import kr.ac.koreatech.sw.kosp.domain.mail.model.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
