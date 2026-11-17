package io.swkoreatech.kosp.domain.organization.post.model;

import java.util.ArrayList;
import java.util.List;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationPostCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationPostVisibility visibility;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;

    @ElementCollection
    @CollectionTable(name = "organization_post_files", joinColumns = @JoinColumn(name = "post_id"))
    private List<OrganizationPostFile> files = new ArrayList<>();

    @Builder
    private OrganizationPost(
        Organization organization,
        User author,
        OrganizationPostCategory category,
        OrganizationPostVisibility visibility,
        String title,
        String description,
        String content,
        List<OrganizationPostFile> files
    ) {
        this.organization = organization;
        this.author = author;
        this.category = category;
        this.visibility = visibility;
        this.title = title;
        this.description = description;
        this.content = content;
        if (files != null) {
            this.files = files;
        }
    }

    public void incrementCommentsCount() {
        this.commentsCount++;
    }

    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }
}
