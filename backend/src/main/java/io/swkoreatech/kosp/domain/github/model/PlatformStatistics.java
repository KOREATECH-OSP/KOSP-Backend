package io.swkoreatech.kosp.domain.github.model;

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
}
