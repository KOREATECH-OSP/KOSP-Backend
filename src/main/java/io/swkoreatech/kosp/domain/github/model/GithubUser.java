package io.swkoreatech.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.swkoreatech.kosp.global.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.data.domain.Persistable;

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
    private String githubLogin; // username (ex. byungKHee)

    @Column(name = "github_name")
    private String githubName; // profile name (ex. 강병희)

    @Column(name = "github_avatar_url")
    private String githubAvatarUrl;

    @Column(name = "github_token", columnDefinition = "TEXT")
    private String githubToken; // Access Token

    @Builder.Default
    @Column(name = "last_crawling")
    private LocalDateTime lastCrawling = LocalDateTime.now();

    public void updateLastCrawling() {
        this.lastCrawling = LocalDateTime.now();
    }

    public void updateProfile(String githubLogin, String githubName, String githubAvatarUrl, String githubToken) {
        this.githubLogin = githubLogin;
        this.githubName = githubName;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubToken = githubToken;
    }

    public void updateAvatarUrl(String githubAvatarUrl) {
        this.githubAvatarUrl = githubAvatarUrl;
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
