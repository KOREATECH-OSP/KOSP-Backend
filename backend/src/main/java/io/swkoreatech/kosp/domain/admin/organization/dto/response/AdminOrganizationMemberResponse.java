package io.swkoreatech.kosp.domain.admin.organization.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.organization.model.OrganizationMember;

public record AdminOrganizationMemberResponse(
    Long id,
    Long githubUserId,
    String githubUsername,
    String role,
    String status,
    Long userId,
    LocalDateTime joinedAt,
    LocalDateTime syncedAt
) {
    public static AdminOrganizationMemberResponse from(OrganizationMember member) {
        return new AdminOrganizationMemberResponse(
            member.getId(),
            member.getGithubUserId(),
            member.getGithubUsername(),
            member.getRole().name(),
            member.getStatus().name(),
            member.getUserId(),
            member.getJoinedAt(),
            member.getSyncedAt()
        );
    }
}
