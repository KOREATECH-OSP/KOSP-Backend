package io.swkoreatech.kosp.domain.admin.organization.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;

public record AdminOrganizationRepoResponse(
    Long id,
    Long githubRepoId,
    String repositoryName,
    String repositoryFullName,
    String repositoryUrl,
    String visibility,
    boolean isActive,
    LocalDateTime syncedAt
) {
    public static AdminOrganizationRepoResponse from(OrganizationRepo repo) {
        return new AdminOrganizationRepoResponse(
            repo.getId(),
            repo.getGithubRepoId(),
            repo.getRepositoryName(),
            repo.getRepositoryFullName(),
            repo.getRepositoryUrl(),
            repo.getVisibility(),
            repo.isActive(),
            repo.getSyncedAt()
        );
    }
}
