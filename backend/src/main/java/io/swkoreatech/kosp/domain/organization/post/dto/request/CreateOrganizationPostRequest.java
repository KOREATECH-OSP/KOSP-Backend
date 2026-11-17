package io.swkoreatech.kosp.domain.organization.post.dto.request;

import java.util.List;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostFile;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationPostRequest(
    @NotNull OrganizationPostCategory category,
    @NotNull OrganizationPostVisibility visibility,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 500) String description,
    @NotBlank String content,
    List<FileDto> files
) {
    public record FileDto(
        @NotBlank @Size(max = 300) String fileName,
        @NotBlank @Size(max = 500) String fileUrl
    ) {
        public OrganizationPostFile toEntity() {
            return new OrganizationPostFile(fileName, fileUrl);
        }
    }
}
