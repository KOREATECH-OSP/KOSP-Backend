package io.swkoreatech.kosp.domain.organization.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.organization.model.Organization;

public record OrganizationDetailResponse(
    Long id,
    String githubOrgName,
    String displayName,
    String avatarUrl,
    String status,
    int totalMemberCount,
    int linkedMemberCount,
    int repositoryCount,
    LocalDateTime createdAt
) {
    public static OrganizationDetailResponse from(
        Organization organization,
        int totalMemberCount,
        int linkedMemberCount,
        int repositoryCount
    ) {
        return new OrganizationDetailResponse(
            organization.getId(),
            organization.getGithubOrgName(),
            organization.getDisplayName(),
            organization.getAvatarUrl(),
            organization.getStatus().name(),
            totalMemberCount,
            linkedMemberCount,
            repositoryCount,
            organization.getCreatedAt()
        );
    }
}
