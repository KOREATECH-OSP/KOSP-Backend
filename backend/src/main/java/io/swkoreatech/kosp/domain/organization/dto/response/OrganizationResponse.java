package io.swkoreatech.kosp.domain.organization.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.organization.model.Organization;

public record OrganizationResponse(
    Long id,
    Long githubOrgId,
    String githubOrgName,
    String displayName,
    String avatarUrl,
    String status,
    LocalDateTime createdAt
) {
    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
            organization.getId(),
            organization.getGithubOrgId(),
            organization.getGithubOrgName(),
            organization.getDisplayName(),
            organization.getAvatarUrl(),
            organization.getStatus().name(),
            organization.getCreatedAt()
        );
    }
}
