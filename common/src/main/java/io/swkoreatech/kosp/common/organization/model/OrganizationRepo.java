package io.swkoreatech.kosp.common.organization.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "organization_repositories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationRepo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "github_repo_id", nullable = false, unique = true)
    private Long githubRepoId;

    @Column(name = "repository_name", nullable = false)
    private String repositoryName;

    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @Builder
    private OrganizationRepo(
        Organization organization,
        Long githubRepoId,
        String repositoryName,
        String repositoryFullName,
        String repositoryUrl,
        String visibility,
        LocalDateTime syncedAt
    ) {
        this.organization = organization;
        this.githubRepoId = githubRepoId;
        this.repositoryName = repositoryName;
        this.repositoryFullName = repositoryFullName;
        this.repositoryUrl = repositoryUrl;
        this.visibility = visibility;
        this.syncedAt = syncedAt;
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
