package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
}
