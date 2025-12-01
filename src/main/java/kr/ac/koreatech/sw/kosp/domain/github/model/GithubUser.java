package kr.ac.koreatech.sw.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class GithubUser extends BaseEntity {

    @Id
    @Column(name = "github_id")
    private Long githubId;

    @Column(name = "github_login")
    private String githubLogin; // username (ex. byungKHee)

    @Column(name = "github_name")
    private String githubName; // profile name (ex. 강병희)

    @Setter
    @Column(name = "github_avatar_url")
    private String githubAvatarUrl;

    @Setter
    @Column(name = "github_token", columnDefinition = "TEXT")
    private String githubToken; // Access Token

    @Column(name = "last_crawling")
    private LocalDateTime lastCrawling;

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
        updateLastCrawling();
    }

    public void updateLastCrawling() {
        this.lastCrawling = LocalDateTime.now();
    }

    public void updateProfile(String githubLogin, String githubName, String githubAvatarUrl, String githubToken) {
        this.githubLogin = githubLogin;
        this.githubName = githubName;
        this.githubAvatarUrl = githubAvatarUrl;
        this.githubToken = githubToken;
    }

}
