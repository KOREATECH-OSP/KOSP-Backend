package io.swkoreatech.kosp.domain.admin.contact.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.contact.api.AdminContactApi;
import io.swkoreatech.kosp.domain.admin.contact.dto.request.AdminContactUpdateRequest;
import io.swkoreatech.kosp.domain.admin.contact.dto.response.AdminContactResponse;
import io.swkoreatech.kosp.domain.admin.contact.service.AdminContactService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminContactController implements AdminContactApi {

    private final AdminContactService adminContactService;

    @Override
    @Permit(permitAll = true, name = "contact:read", description = "관리자 연락처 조회")
    public ResponseEntity<AdminContactResponse> getContact() {
        return ResponseEntity.ok(adminContactService.getContact());
    }

    @Override
    @Permit(name = "contact:update", description = "관리자 연락처 수정")
    public ResponseEntity<AdminContactResponse> updateContact(AdminContactUpdateRequest request) {
        return ResponseEntity.ok(adminContactService.updateContact(request));
    }
}
