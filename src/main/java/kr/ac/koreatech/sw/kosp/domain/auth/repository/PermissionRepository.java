package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
