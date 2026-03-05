package io.swkoreatech.kosp.domain.admin.contact.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.contact.dto.request.AdminContactUpdateRequest;
import io.swkoreatech.kosp.domain.admin.contact.dto.response.AdminContactResponse;
import jakarta.validation.Valid;

/**
 * 관리자 연락처 관리 API 인터페이스.
 * <p>Footer에 표시되는 관리자 이메일의 조회 및 수정 기능을 정의한다.</p>
 */
@Tag(name = "Admin - Contact", description = "관리자 연락처 관리 API")
@RequestMapping("/v1/admin/contact")
public interface AdminContactApi {

    /**
     * 관리자 연락처를 조회한다.
     *
     * @return 관리자 연락처 응답
     */
    @Operation(
        summary = "관리자 연락처 조회",
        description = "Footer에 표시할 관리자 이메일을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<AdminContactResponse> getContact();

    /**
     * 관리자 연락처를 수정한다.
     *
     * @param request 연락처 수정 요청
     * @return 수정된 연락처 응답
     */
    @Operation(
        summary = "관리자 연락처 수정",
        description = "Footer에 표시할 관리자 이메일을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping
    ResponseEntity<AdminContactResponse> updateContact(
        @RequestBody @Valid AdminContactUpdateRequest request
    );
}
