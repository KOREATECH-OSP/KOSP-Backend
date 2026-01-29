package io.swkoreatech.kosp.domain.admin.contact.service;

import io.swkoreatech.kosp.domain.admin.contact.dto.request.AdminContactUpdateRequest;
import io.swkoreatech.kosp.domain.admin.contact.dto.response.AdminContactResponse;
import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;
import io.swkoreatech.kosp.domain.admin.contact.repository.AdminContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContactService {

    private final AdminContactRepository adminContactRepository;

    public AdminContactResponse getContact() {
        AdminContact contact = adminContactRepository.getOrCreate();
        return AdminContactResponse.from(contact);
    }

    @Transactional
    public AdminContactResponse updateContact(AdminContactUpdateRequest request) {
        AdminContact contact = adminContactRepository.getOrCreate();
        contact.updateEmail(request.email());
        return AdminContactResponse.from(contact);
    }
}
