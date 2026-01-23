package io.swkoreatech.kosp.common.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BasePlatformStatistics {

    @Id
    @Column(name = "stat_key", length = 50)
    private String statKey;

    @Column(name = "avg_commit_count", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgCommitCount = BigDecimal.ZERO;

    @Column(name = "avg_star_count", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgStarCount = BigDecimal.ZERO;

    @Column(name = "avg_pr_count", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgPrCount = BigDecimal.ZERO;

    @Column(name = "avg_issue_count", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgIssueCount = BigDecimal.ZERO;

    @Column(name = "total_user_count", nullable = false)
    private Integer totalUserCount = 0;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    protected void initializeStatKey(String statKey) {
        this.statKey = statKey;
        this.calculatedAt = LocalDateTime.now();
    }

    public void updateAverages(
        BigDecimal avgCommitCount,
        BigDecimal avgStarCount,
        BigDecimal avgPrCount,
        BigDecimal avgIssueCount,
        Integer totalUserCount
    ) {
        this.avgCommitCount = avgCommitCount;
        this.avgStarCount = avgStarCount;
        this.avgPrCount = avgPrCount;
        this.avgIssueCount = avgIssueCount;
        this.totalUserCount = totalUserCount;
        this.calculatedAt = LocalDateTime.now();
    }
}
