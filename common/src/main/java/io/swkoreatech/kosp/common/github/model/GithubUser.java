package io.swkoreatech.kosp.common.github.model;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.data.domain.Persistable;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

    @Column(name = "rate_limit_reset_at")
    private Instant rateLimitResetAt;

    @Transient
    private Integer rateLimitRemaining;

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

    public void updateRateLimit(Instant resetAt, Integer remaining) {
        this.rateLimitResetAt = resetAt;
        this.rateLimitRemaining = remaining;
    }

    public boolean isRateLimitExpired() {
        if (rateLimitResetAt == null) {
            return true;
        }
        return Instant.now().isAfter(rateLimitResetAt);
    }

    public Integer getRemainingOrDefault() {
        return rateLimitRemaining != null ? rateLimitRemaining : 5000;
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
