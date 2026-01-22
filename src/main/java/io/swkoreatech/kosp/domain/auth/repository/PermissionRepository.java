package io.swkoreatech.kosp.domain.auth.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

public interface PermissionRepository extends Repository<Permission, Long> {
    Permission save(Permission permission);
    List<Permission> findAll();
    java.util.Optional<Permission> findByName(String name);
    void deleteByName(String name);

    default Permission getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
