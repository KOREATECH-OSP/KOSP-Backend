package kr.ac.koreatech.sw.kosp.domain.github.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_global_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubGlobalStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "avg_commit_count", nullable = false)
    private Double avgCommitCount;

    @Column(name = "avg_star_count", nullable = false)
    private Double avgStarCount;

    @Column(name = "avg_pr_count", nullable = false)
    private Double avgPrCount;

    @Column(name = "avg_issue_count", nullable = false)
    private Double avgIssueCount;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public static GithubGlobalStatistics create(
        Double avgCommitCount,
        Double avgStarCount,
        Double avgPrCount,
        Double avgIssueCount,
        Integer totalUsers
    ) {
        GithubGlobalStatistics stats = new GithubGlobalStatistics();
        stats.avgCommitCount = avgCommitCount;
        stats.avgStarCount = avgStarCount;
        stats.avgPrCount = avgPrCount;
        stats.avgIssueCount = avgIssueCount;
        stats.totalUsers = totalUsers;
        stats.calculatedAt = LocalDateTime.now();
        return stats;
    }
}
