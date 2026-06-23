package io.swkoreatech.kosp.domain.organization.dto.response;

import io.swkoreatech.kosp.infra.github.dto.GithubOrgMembership;

public record AvailableOrganizationResponse(
    Long githubOrgId,
    String githubOrgName,
    String avatarUrl,
    boolean alreadyRegistered
) {
    public static AvailableOrganizationResponse from(GithubOrgMembership membership, boolean alreadyRegistered) {
        return new AvailableOrganizationResponse(
            membership.organization().id(),
            membership.organization().login(),
            membership.organization().avatarUrl(),
            alreadyRegistered
        );
    }
}
