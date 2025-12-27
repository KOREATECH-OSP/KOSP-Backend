package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(String name);
}
