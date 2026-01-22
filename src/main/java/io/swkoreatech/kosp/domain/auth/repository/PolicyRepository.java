package io.swkoreatech.kosp.domain.auth.repository;

import java.util.Optional;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

import org.springframework.data.repository.Repository;

public interface PolicyRepository extends Repository<Policy, Long> {
    Policy save(Policy policy);
    Optional<Policy> findByName(String name);
    java.util.List<Policy> findAll();
    void deleteByName(String name);

    default Policy getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
