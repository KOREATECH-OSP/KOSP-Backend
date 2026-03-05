package io.swkoreatech.kosp.domain.admin.contact.repository;

import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;

import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 관리자 연락처 저장소.
 * <p>관리자 연락처의 조회 및 저장 기능을 제공한다.</p>
 */
public interface AdminContactRepository extends Repository<AdminContact, Long> {

    /**
     * 식별자로 관리자 연락처를 조회한다.
     *
     * @param id 관리자 연락처 식별자
     * @return 관리자 연락처 Optional
     */
    Optional<AdminContact> findById(Long id);

    /**
     * 관리자 연락처를 저장한다.
     *
     * @param adminContact 저장할 관리자 연락처
     * @return 저장된 관리자 연락처
     */
    AdminContact save(AdminContact adminContact);

    /**
     * 관리자 연락처를 조회하거나, 없으면 기본값을 생성하여 반환한다.
     *
     * @return 관리자 연락처 엔티티
     */
    default AdminContact getOrCreate() {
        return findById(1L).orElseGet(() -> save(AdminContact.createDefault()));
    }
}
