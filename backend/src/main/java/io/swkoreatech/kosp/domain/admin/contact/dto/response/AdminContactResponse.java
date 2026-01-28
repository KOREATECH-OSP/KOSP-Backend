package io.swkoreatech.kosp.domain.admin.contact.dto.response;

import io.swkoreatech.kosp.domain.admin.contact.model.AdminContact;

public record AdminContactResponse(
    String email
) {
    public static AdminContactResponse from(AdminContact contact) {
        return new AdminContactResponse(contact.getEmail());
    }
}
