package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;

public interface PermissionRepository extends Repository<Permission, Long> {
    Permission save(Permission permission);
    List<Permission> findAll();
}
