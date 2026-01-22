package io.swkoreatech.kosp.harvester.statistics.model;

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
    private Integer totalCommits = 0;

    @Column(nullable = false)
    private Integer totalLines = 0;

    @Column(nullable = false)
    private Integer totalAdditions = 0;

    @Column(nullable = false)
    private Integer totalDeletions = 0;

    @Column(nullable = false)
    private Integer totalPrs = 0;

    @Column(nullable = false)
    private Integer totalIssues = 0;

    @Column(nullable = false)
    private Integer ownedReposCount = 0;

    @Column(nullable = false)
    private Integer contributedReposCount = 0;

    @Column(nullable = false)
    private Integer totalStarsReceived = 0;

    @Column(nullable = false)
    private Integer totalForksReceived = 0;

    @Column(nullable = false)
    private Integer nightCommits = 0;

    @Column(nullable = false)
    private Integer dayCommits = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mainRepoScore = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal otherRepoScore = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prIssueScore = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal reputationScore = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    private LocalDate dataPeriodStart;

    private LocalDate dataPeriodEnd;

    public static GithubUserStatistics create(String githubId) {
        GithubUserStatistics statistics = new GithubUserStatistics();
        statistics.githubId = githubId;
        statistics.calculatedAt = LocalDateTime.now();
        return statistics;
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

    public void updateDetailedScore(
        BigDecimal mainRepoScore,
        BigDecimal otherRepoScore,
        BigDecimal prIssueScore,
        BigDecimal reputationScore
    ) {
        this.mainRepoScore = mainRepoScore;
        this.otherRepoScore = otherRepoScore;
        this.prIssueScore = prIssueScore;
        this.reputationScore = reputationScore;
        this.totalScore = mainRepoScore
            .add(otherRepoScore)
            .add(prIssueScore)
            .add(reputationScore);
    }

    public void updateDataPeriod(LocalDate start, LocalDate end) {
        this.dataPeriodStart = start;
        this.dataPeriodEnd = end;
    }
}
