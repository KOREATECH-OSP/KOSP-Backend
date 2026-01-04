package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import org.springframework.data.repository.Repository;

public interface PermissionRepository extends Repository<Permission, Long> {
    Permission save(Permission permission);
    List<Permission> findAll();
}
