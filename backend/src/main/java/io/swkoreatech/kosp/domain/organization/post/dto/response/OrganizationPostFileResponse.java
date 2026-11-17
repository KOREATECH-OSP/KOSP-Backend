package io.swkoreatech.kosp.domain.organization.post.dto.response;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostFile;

public record OrganizationPostFileResponse(
    String fileName,
    String fileUrl
) {
    public static OrganizationPostFileResponse from(OrganizationPostFile file) {
        return new OrganizationPostFileResponse(file.getFileName(), file.getFileUrl());
    }
}
