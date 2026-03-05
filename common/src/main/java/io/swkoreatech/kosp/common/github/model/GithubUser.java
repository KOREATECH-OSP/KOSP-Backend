package io.swkoreatech.kosp.common.github.model;

import io.swkoreatech.kosp.common.model.BaseEntity;

import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GitHub 사용자 엔티티.
 *
 * <p>GitHub OAuth로 연동된 사용자의 프로필 정보와 API 토큰을 관리한다.
 * GitHub API 호출 시의 Rate Limit 상태도 추적한다.</p>
 */
@Entity
@Getter
@Table(name = "github_user")
@NoArgsConstructor
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

    @Column(name = "last_crawling")
    private LocalDateTime lastCrawling = LocalDateTime.now();

    @Column(name = "rate_limit_reset_at")
    private Instant rateLimitResetAt;

    @Transient
    private Integer rateLimitRemaining;

    @Builder
    private GithubUser(
            Long githubId,
            String githubLogin,
            String githubName,
            String githubAvatarUrl,
            String githubToken
    ) {
        this.githubId = githubId;
        this.githubLogin = githubLogin;
        this.githubName = githubName;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubToken = githubToken;
    }

    /**
     * 마지막 크롤링 시각을 현재 시각으로 갱신한다.
     */
    public void updateLastCrawling() {
        this.lastCrawling = LocalDateTime.now();
    }

    /**
     * GitHub 프로필 정보를 갱신한다.
     *
     * @param githubLogin     GitHub 로그인 ID
     * @param githubName      GitHub 표시 이름
     * @param githubAvatarUrl GitHub 아바타 이미지 URL
     * @param githubToken     GitHub API 토큰
     */
    public void updateProfile(String githubLogin, String githubName, String githubAvatarUrl, String githubToken) {
        this.githubLogin = githubLogin;
        this.githubName = githubName;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubToken = githubToken;
    }

    /**
     * GitHub 아바타 URL을 갱신한다.
     *
     * @param githubAvatarUrl 새로운 아바타 이미지 URL
     */
    public void updateAvatarUrl(String githubAvatarUrl) {
        this.githubAvatarUrl = githubAvatarUrl;
    }

    /**
     * GitHub API Rate Limit 정보를 갱신한다.
     *
     * @param resetAt   Rate Limit 초기화 시각
     * @param remaining 남은 API 호출 횟수
     */
    public void updateRateLimit(Instant resetAt, Integer remaining) {
        this.rateLimitResetAt = resetAt;
        this.rateLimitRemaining = remaining;
    }

    /**
     * Rate Limit 제한 시간이 만료되었는지 확인한다.
     *
     * @return 만료되었거나 설정되지 않았으면 {@code true}
     */
    public boolean isRateLimitExpired() {
        if (rateLimitResetAt == null) {
            return true;
        }
        return Instant.now().isAfter(rateLimitResetAt);
    }

    /**
     * 남은 API 호출 횟수를 반환한다. 설정되지 않은 경우 기본값 5000을 반환한다.
     *
     * @return 남은 API 호출 횟수
     */
    public Integer getRemainingOrDefault() {
        return rateLimitRemaining != null ? rateLimitRemaining : 5000;
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return githubId;
    }

    /** 신규 엔티티 여부를 반환한다. */
    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
