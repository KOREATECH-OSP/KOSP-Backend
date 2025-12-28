package kr.ac.koreatech.sw.kosp.domain.github.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubStats {

    @Column(name = "total_commits", nullable = false)
    private Long totalCommits = 0L;

    @Column(name = "total_prs", nullable = false)
    private Long totalPrs = 0L;

    @Column(name = "total_issues", nullable = false)
    private Long totalIssues = 0L;

    @Column(name = "total_stars", nullable = false)
    private Long totalStars = 0L;

    @Column(name = "repo_count", nullable = false)
    private Long repoCount = 0L;

    @Builder
    private GithubStats(Long totalCommits, Long totalPrs, Long totalIssues, Long totalStars, Long repoCount) {
        this.totalCommits = totalCommits != null ? totalCommits : 0L;
        this.totalPrs = totalPrs != null ? totalPrs : 0L;
        this.totalIssues = totalIssues != null ? totalIssues : 0L;
        this.totalStars = totalStars != null ? totalStars : 0L;
        this.repoCount = repoCount != null ? repoCount : 0L;
    }
}
