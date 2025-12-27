package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import org.springframework.data.repository.Repository;

public interface PolicyRepository extends Repository<Policy, Long> {
    Policy save(Policy policy);
    Optional<Policy> findByName(String name);
}
