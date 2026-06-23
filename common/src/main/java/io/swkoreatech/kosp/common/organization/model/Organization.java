package io.swkoreatech.kosp.common.organization.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "organizations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_org_id", nullable = false, unique = true)
    private Long githubOrgId;

    @Column(name = "github_org_name", nullable = false)
    private String githubOrgName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "registered_by_user_id", nullable = false)
    private Long registeredByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status;

    @Builder
    private Organization(
        Long githubOrgId,
        String githubOrgName,
        String displayName,
        String avatarUrl,
        Long registeredByUserId
    ) {
        this.githubOrgId = githubOrgId;
        this.githubOrgName = githubOrgName;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.registeredByUserId = registeredByUserId;
        this.status = OrganizationStatus.ACTIVE;
    }

    public void disconnect() {
        this.status = OrganizationStatus.DISCONNECTED;
    }

    public void activate() {
        this.status = OrganizationStatus.ACTIVE;
    }
}
