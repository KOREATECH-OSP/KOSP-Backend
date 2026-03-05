package io.swkoreatech.kosp.domain.admin.contact.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.contact.dto.request.AdminContactUpdateRequest;
import io.swkoreatech.kosp.domain.admin.contact.dto.response.AdminContactResponse;
import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;
import io.swkoreatech.kosp.domain.admin.contact.repository.AdminContactRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 연락처 서비스.
 * <p>관리자 연락처 조회 및 수정 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContactService {

    private final AdminContactRepository adminContactRepository;

    /**
     * 관리자 연락처를 조회한다.
     *
     * @return 관리자 연락처 응답 DTO
     */
    public AdminContactResponse getContact() {
        AdminContact contact = adminContactRepository.getOrCreate();
        return AdminContactResponse.from(contact);
    }

    /**
     * 관리자 연락처를 수정한다.
     *
     * @param request 연락처 수정 요청 DTO
     * @return 수정된 관리자 연락처 응답 DTO
     */
    @Transactional
    public AdminContactResponse updateContact(AdminContactUpdateRequest request) {
        AdminContact contact = adminContactRepository.getOrCreate();
        contact.updateEmail(request.email());
        return AdminContactResponse.from(contact);
    }
}
