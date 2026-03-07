package io.swkoreatech.kosp.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

/**
 * 정책(Policy) 저장소.
 * <p>정책의 저장, 조회, 삭제 기능을 제공한다.</p>
 */
public interface PolicyRepository extends Repository<Policy, Long> {

    /**
     * 정책을 저장한다.
     *
     * @param policy 저장할 정책
     * @return 저장된 정책
     */
    Policy save(Policy policy);

    /**
     * 이름으로 정책을 조회한다.
     *
     * @param name 정책 이름
     * @return 정책 Optional
     */
    Optional<Policy> findByName(String name);

    /**
     * 모든 정책 목록을 조회한다.
     *
     * @return 정책 목록
     */
    java.util.List<Policy> findAll();

    /**
     * 이름으로 정책을 삭제한다.
     *
     * @param name 삭제할 정책 이름
     */
    void deleteByName(String name);

    /**
     * 이름으로 정책을 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param name 정책 이름
     * @return 정책 엔티티
     * @throws GlobalException 정책을 찾을 수 없는 경우
     */
    default Policy getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new GlobalException(
                ExceptionMessage.NOT_FOUND));
    }
}
