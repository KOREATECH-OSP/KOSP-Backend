package io.swkoreatech.kosp.common.github.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_user_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubUserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String githubId;

    @Column(nullable = false)
    private Integer totalCommits;

    @Column(nullable = false)
    private Integer totalLines;

    @Column(nullable = false)
    private Integer totalAdditions;

    @Column(nullable = false)
    private Integer totalDeletions;

    @Column(nullable = false)
    private Integer totalPrs;

    @Column(nullable = false)
    private Integer totalIssues;

    @Column(nullable = false)
    private Integer ownedReposCount;

    @Column(nullable = false)
    private Integer contributedReposCount;

    @Column(nullable = false)
    private Integer totalStarsReceived;

    @Column(nullable = false)
    private Integer totalForksReceived;

    @Column(nullable = false)
    private Integer nightCommits;

    @Column(nullable = false)
    private Integer dayCommits;

    @Column(name = "activity_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal activityScore;

    @Column(name = "diversity_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal diversityScore;

    @Column(name = "impact_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal impactScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalScore;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    private LocalDate dataPeriodStart;

    private LocalDate dataPeriodEnd;

    @Builder
    private GithubUserStatistics(
        String githubId,
        Integer totalCommits,
        Integer totalLines,
        Integer totalAdditions,
        Integer totalDeletions,
        Integer totalPrs,
        Integer totalIssues,
        Integer ownedReposCount,
        Integer contributedReposCount,
        Integer totalStarsReceived,
        Integer totalForksReceived,
        Integer nightCommits,
        Integer dayCommits,
        BigDecimal activityScore,
        BigDecimal diversityScore,
        BigDecimal impactScore,
        BigDecimal totalScore,
        LocalDateTime calculatedAt,
        LocalDate dataPeriodStart,
        LocalDate dataPeriodEnd
    ) {
        this.githubId = githubId;
        this.totalCommits = totalCommits != null ? totalCommits : 0;
        this.totalLines = totalLines != null ? totalLines : 0;
        this.totalAdditions = totalAdditions != null ? totalAdditions : 0;
        this.totalDeletions = totalDeletions != null ? totalDeletions : 0;
        this.totalPrs = totalPrs != null ? totalPrs : 0;
        this.totalIssues = totalIssues != null ? totalIssues : 0;
        this.ownedReposCount = ownedReposCount != null ? ownedReposCount : 0;
        this.contributedReposCount = contributedReposCount != null ? contributedReposCount : 0;
        this.totalStarsReceived = totalStarsReceived != null ? totalStarsReceived : 0;
        this.totalForksReceived = totalForksReceived != null ? totalForksReceived : 0;
        this.nightCommits = nightCommits != null ? nightCommits : 0;
        this.dayCommits = dayCommits != null ? dayCommits : 0;
        this.activityScore = activityScore != null ? activityScore : BigDecimal.ZERO;
        this.diversityScore = diversityScore != null ? diversityScore : BigDecimal.ZERO;
        this.impactScore = impactScore != null ? impactScore : BigDecimal.ZERO;
        this.totalScore = totalScore != null ? totalScore : BigDecimal.ZERO;
        this.calculatedAt = calculatedAt != null ? calculatedAt : LocalDateTime.now();
        this.dataPeriodStart = dataPeriodStart;
        this.dataPeriodEnd = dataPeriodEnd;
    }

    public void updateStatistics(
        Integer totalCommits,
        Integer totalLines,
        Integer totalAdditions,
        Integer totalDeletions,
        Integer totalPrs,
        Integer totalIssues,
        Integer ownedReposCount,
        Integer contributedReposCount,
        Integer totalStarsReceived,
        Integer totalForksReceived,
        Integer nightCommits,
        Integer dayCommits
    ) {
        this.totalCommits = totalCommits;
        this.totalLines = totalLines;
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
        this.totalPrs = totalPrs;
        this.totalIssues = totalIssues;
        this.ownedReposCount = ownedReposCount;
        this.contributedReposCount = contributedReposCount;
        this.totalStarsReceived = totalStarsReceived;
        this.totalForksReceived = totalForksReceived;
        this.nightCommits = nightCommits;
        this.dayCommits = dayCommits;
        this.calculatedAt = LocalDateTime.now();
    }

    public void updateScores(
        BigDecimal activityScore,
        BigDecimal diversityScore,
        BigDecimal impactScore
    ) {
        this.activityScore = activityScore;
        this.diversityScore = diversityScore;
        this.impactScore = impactScore;
        this.totalScore = activityScore.add(diversityScore).add(impactScore);
    }

    public void updateDataPeriod(LocalDate start, LocalDate end) {
        this.dataPeriodStart = start;
        this.dataPeriodEnd = end;
    }
}
