package io.swkoreatech.kosp.domain.admin.contact.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.contact.dto.request.AdminContactUpdateRequest;
import io.swkoreatech.kosp.domain.admin.contact.dto.response.AdminContactResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Contact", description = "관리자 연락처 관리 API")
@RequestMapping("/v1/admin/contact")
public interface AdminContactApi {

    @Operation(summary = "관리자 연락처 조회", description = "Footer에 표시할 관리자 이메일을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<AdminContactResponse> getContact();

    @Operation(summary = "관리자 연락처 수정", description = "Footer에 표시할 관리자 이메일을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping
    ResponseEntity<AdminContactResponse> updateContact(
        @RequestBody @Valid AdminContactUpdateRequest request
    );
}
