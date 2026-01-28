package io.swkoreatech.kosp.domain.admin.contact.repository;

import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface AdminContactRepository extends Repository<AdminContact, Long> {

    Optional<AdminContact> findById(Long id);

    AdminContact save(AdminContact adminContact);

    default AdminContact getOrCreate() {
        return findById(1L).orElseGet(() -> save(AdminContact.createDefault()));
    }
}
