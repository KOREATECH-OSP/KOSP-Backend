package io.swkoreatech.kosp.domain.admin.contact.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 관리자 연락처 수정 요청 DTO.
 *
 * @param email 수정할 이메일 주소
 */
public record AdminContactUpdateRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email
) {}
