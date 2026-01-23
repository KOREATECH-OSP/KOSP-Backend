package io.swkoreatech.kosp.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

import org.springframework.data.repository.Repository;

public interface RoleRepository extends Repository<Role, Long> {
    Role save(Role role);
    List<Role> findAll();
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);

    default Role getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
