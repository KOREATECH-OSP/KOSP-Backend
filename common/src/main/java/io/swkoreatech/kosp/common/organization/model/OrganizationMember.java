package io.swkoreatech.kosp.common.organization.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "organization_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "github_user_id", nullable = false)
    private Long githubUserId;

    @Column(name = "github_username", nullable = false)
    private String githubUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private OrganizationMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationMemberStatus status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @Builder
    private OrganizationMember(
        Organization organization,
        Long userId,
        Long githubUserId,
        String githubUsername,
        OrganizationMemberRole role,
        OrganizationMemberStatus status,
        LocalDateTime syncedAt
    ) {
        this.organization = organization;
        this.userId = userId;
        this.githubUserId = githubUserId;
        this.githubUsername = githubUsername;
        this.role = role;
        this.syncedAt = syncedAt;
        this.status = status != null ? status : (userId != null ? OrganizationMemberStatus.LINKED : OrganizationMemberStatus.NOT_JOINED);
        this.joinedAt = this.status == OrganizationMemberStatus.LINKED ? LocalDateTime.now() : null;
    }

    public void link(Long linkedUserId) {
        this.userId = linkedUserId;
        this.status = OrganizationMemberStatus.LINKED;
        this.joinedAt = LocalDateTime.now();
    }

    public void remove() {
        this.status = OrganizationMemberStatus.REMOVED;
    }

    public void appointAdmin() {
        this.role = OrganizationMemberRole.ADMIN;
    }

    public void demoteToMember() {
        this.role = OrganizationMemberRole.MEMBER;
    }

    public void syncGithubRole(boolean isGithubAdmin) {
        if (isGithubAdmin) {
            this.role = OrganizationMemberRole.OWNER;
        } else if (this.role == OrganizationMemberRole.OWNER) {
            this.role = OrganizationMemberRole.MEMBER;
        }
        // ADMIN과 MEMBER는 GitHub 동기화로 변경하지 않음
    }
}
