package io.swkoreatech.kosp.harvester.user;

import java.time.LocalDateTime;

import org.springframework.data.domain.Persistable;

import io.swkoreatech.kosp.harvester.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "github_user")
@NoArgsConstructor
@SuperBuilder
public class GithubUser extends BaseEntity implements Persistable<Long> {

    @Id
    @Column(name = "github_id")
    private Long githubId;

    @Column(name = "github_login")
    private String githubLogin;

    @Column(name = "github_name")
    private String githubName;

    @Column(name = "github_avatar_url")
    private String githubAvatarUrl;

    @Column(name = "github_token", columnDefinition = "TEXT")
    private String githubToken;

    @Builder.Default
    @Column(name = "last_crawling")
    private LocalDateTime lastCrawling = LocalDateTime.now();

    public void updateLastCrawling() {
        this.lastCrawling = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return githubId;
    }

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
