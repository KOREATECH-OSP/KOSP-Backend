package io.swkoreatech.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "github_repository_statistics",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_repo_contributor",
        columnNames = {"repo_owner", "repo_name", "contributor_github_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubRepositoryStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_owner", nullable = false, length = 100)
    private String repoOwner;

    @Column(name = "repo_name", nullable = false, length = 200)
    private String repoName;

    @Column(name = "contributor_github_id", nullable = false, length = 100)
    private String contributorGithubId;

    @Column(name = "is_owned")
    private Boolean isOwned = false;

    // 저장소 기본 정보
    @Column(name = "stargazers_count", nullable = false)
    private Integer stargazersCount = 0;

    @Column(name = "forks_count", nullable = false)
    private Integer forksCount = 0;

    @Column(name = "watchers_count", nullable = false)
    private Integer watchersCount = 0;

    // 저장소 전체 통계
    @Column(name = "total_commits_count", nullable = false)
    private Integer totalCommitsCount = 0;

    @Column(name = "total_prs_count", nullable = false)
    private Integer totalPrsCount = 0;

    @Column(name = "total_issues_count", nullable = false)
    private Integer totalIssuesCount = 0;

    // 사용자별 기여도
    @Column(name = "user_commits_count", nullable = false)
    private Integer userCommitsCount = 0;

    @Column(name = "user_prs_count", nullable = false)
    private Integer userPrsCount = 0;

    @Column(name = "user_issues_count", nullable = false)
    private Integer userIssuesCount = 0;

    @Column(name = "last_commit_date")
    private LocalDateTime lastCommitDate;

    @Column(length = 500)
    private String description;

    @Column(name = "primary_language", length = 50)
    private String primaryLanguage;

    @Column(name = "repo_created_at")
    private LocalDateTime repoCreatedAt;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public static GithubRepositoryStatistics create(
        String repoOwner,
        String repoName,
        String contributorGithubId
    ) {
        GithubRepositoryStatistics statistics = new GithubRepositoryStatistics();
        statistics.repoOwner = repoOwner;
        statistics.repoName = repoName;
        statistics.contributorGithubId = contributorGithubId;
        statistics.calculatedAt = LocalDateTime.now();
        return statistics;
    }

    public void updateRepositoryInfo(
        Integer stargazersCount,
        Integer forksCount,
        Integer watchersCount,
        String description,
        String primaryLanguage,
        LocalDateTime repoCreatedAt
    ) {
        this.stargazersCount = stargazersCount;
        this.forksCount = forksCount;
        this.watchersCount = watchersCount;
        this.description = description;
        this.primaryLanguage = primaryLanguage;
        this.repoCreatedAt = repoCreatedAt;
    }

    public void updateOwnership(Boolean isOwned) {
        this.isOwned = isOwned;
    }

    public void updateUserContributions(
        Integer userCommitsCount,
        Integer userPrsCount,
        Integer userIssuesCount,
        LocalDateTime lastCommitDate
    ) {
        this.userCommitsCount = userCommitsCount;
        this.userPrsCount = userPrsCount;
        this.userIssuesCount = userIssuesCount;
        this.lastCommitDate = lastCommitDate;
    }

    public void updateTotalCounts(
        Integer totalCommitsCount,
        Integer totalPrsCount,
        Integer totalIssuesCount
    ) {
        this.totalCommitsCount = totalCommitsCount;
        this.totalPrsCount = totalPrsCount;
        this.totalIssuesCount = totalIssuesCount;
    }
}
