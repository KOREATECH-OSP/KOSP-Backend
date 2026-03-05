package io.swkoreatech.kosp.domain.auth.repository;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 역할(Role) 저장소.
 * <p>역할의 저장, 조회, 삭제 기능을 제공한다.</p>
 */
public interface RoleRepository extends Repository<Role, Long> {

    /**
     * 역할을 저장한다.
     *
     * @param role 저장할 역할
     * @return 저장된 역할
     */
    Role save(Role role);

    /**
     * 모든 역할 목록을 조회한다.
     *
     * @return 역할 목록
     */
    List<Role> findAll();

    /**
     * 이름으로 역할을 조회한다.
     *
     * @param name 역할 이름
     * @return 역할 Optional
     */
    Optional<Role> findByName(String name);

    /**
     * 이름으로 역할의 존재 여부를 확인한다.
     *
     * @param name 역할 이름
     * @return 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 이름으로 역할을 삭제한다.
     *
     * @param name 삭제할 역할 이름
     */
    void deleteByName(String name);

    /**
     * 이름으로 역할을 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param name 역할 이름
     * @return 역할 엔티티
     * @throws GlobalException 역할을 찾을 수 없는 경우
     */
    default Role getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
