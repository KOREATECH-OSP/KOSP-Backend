package io.swkoreatech.kosp.common.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 통계 기반 클래스.
 *
 * <p>GitHub 등 외부 플랫폼 사용자들의 평균 활동 지표(커밋, 스타, PR, 이슈)와
 * 총 사용자 수를 관리하는 추상 엔티티이다.</p>
 */
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

    /**
     * 통계 키를 초기화하고 계산 시각을 현재 시각으로 설정한다.
     *
     * @param statKey 통계 식별 키
     */
    protected void initializeStatKey(String statKey) {
        this.statKey = statKey;
        this.calculatedAt = LocalDateTime.now();
    }

    /**
     * 플랫폼 평균 지표와 총 사용자 수를 갱신한다.
     *
     * @param avgCommitCount 평균 커밋 수
     * @param avgStarCount   평균 스타 수
     * @param avgPrCount     평균 PR 수
     * @param avgIssueCount  평균 이슈 수
     * @param totalUserCount 총 사용자 수
     */
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
