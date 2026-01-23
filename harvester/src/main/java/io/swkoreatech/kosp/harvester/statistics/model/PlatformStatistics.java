package io.swkoreatech.kosp.harvester.statistics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformStatistics {

    @Id
    @Column(length = 50)
    private String statKey;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal avgCommitCount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal avgStarCount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal avgPrCount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal avgIssueCount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalUserCount = 0;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    public static PlatformStatistics create(String statKey) {
        PlatformStatistics stats = new PlatformStatistics();
        stats.statKey = statKey;
        stats.calculatedAt = LocalDateTime.now();
        return stats;
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
