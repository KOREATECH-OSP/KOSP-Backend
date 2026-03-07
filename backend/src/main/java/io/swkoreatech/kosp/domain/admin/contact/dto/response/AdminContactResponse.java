package io.swkoreatech.kosp.domain.admin.contact.dto.response;

import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;

/**
 * 관리자 연락처 응답 DTO.
 *
 * @param email 관리자 이메일 주소
 */
public record AdminContactResponse(
    String email
) {
    /**
     * {@link AdminContact} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param contact 관리자 연락처 엔티티
     * @return 관리자 연락처 응답 DTO
     */
    public static AdminContactResponse from(AdminContact contact) {
        return new AdminContactResponse(contact.getEmail());
    }
}
