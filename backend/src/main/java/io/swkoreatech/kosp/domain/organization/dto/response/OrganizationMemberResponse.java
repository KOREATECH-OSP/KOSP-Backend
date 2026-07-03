package io.swkoreatech.kosp.domain.organization.dto.response;

import io.swkoreatech.kosp.common.organization.model.OrganizationMember;

public record OrganizationMemberResponse(
    Long id,
    String githubUsername,
    String role,
    String status
) {
    public static OrganizationMemberResponse from(OrganizationMember member) {
        return new OrganizationMemberResponse(
            member.getId(),
            member.getGithubUsername(),
            member.getRole().name(),
            member.getStatus().name()
        );
    }
}
