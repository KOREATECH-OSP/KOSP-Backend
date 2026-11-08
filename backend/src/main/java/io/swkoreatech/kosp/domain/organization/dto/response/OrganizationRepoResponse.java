package io.swkoreatech.kosp.domain.organization.dto.response;

import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;

public record OrganizationRepoResponse(
    Long id,
    Long githubRepoId,
    String repositoryName,
    String repositoryFullName,
    String repositoryUrl,
    String visibility,
    boolean isActive
) {
    public static OrganizationRepoResponse from(OrganizationRepo repo) {
        return new OrganizationRepoResponse(
            repo.getId(),
            repo.getGithubRepoId(),
            repo.getRepositoryName(),
            repo.getRepositoryFullName(),
            repo.getRepositoryUrl(),
            repo.getVisibility(),
            repo.isActive()
        );
    }
}
