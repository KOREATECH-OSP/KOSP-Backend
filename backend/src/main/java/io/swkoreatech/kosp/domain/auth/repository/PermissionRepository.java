package io.swkoreatech.kosp.domain.auth.repository;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

import java.util.List;

import org.springframework.data.repository.Repository;

/**
 * 권한(Permission) 저장소.
 * <p>권한의 저장, 조회, 삭제 기능을 제공한다.</p>
 */
public interface PermissionRepository extends Repository<Permission, Long> {

    /**
     * 권한을 저장한다.
     *
     * @param permission 저장할 권한
     * @return 저장된 권한
     */
    Permission save(Permission permission);

    /**
     * 모든 권한 목록을 조회한다.
     *
     * @return 권한 목록
     */
    List<Permission> findAll();

    /**
     * 이름으로 권한을 조회한다.
     *
     * @param name 권한 이름
     * @return 권한 Optional
     */
    java.util.Optional<Permission> findByName(String name);

    /**
     * 이름으로 권한을 삭제한다.
     *
     * @param name 삭제할 권한 이름
     */
    void deleteByName(String name);

    /**
     * 이름으로 권한을 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param name 권한 이름
     * @return 권한 엔티티
     * @throws GlobalException 권한을 찾을 수 없는 경우
     */
    default Permission getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
