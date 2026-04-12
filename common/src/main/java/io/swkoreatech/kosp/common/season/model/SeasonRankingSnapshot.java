package io.swkoreatech.kosp.common.season.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시즌 종료 랭킹 스냅샷 엔티티 (불변 기록).
 *
 * <p>시즌 종료 시 최종 점수·티어·순위를 영구 보존한다.</p>
 */
@Getter
@Entity
@Table(name = "season_ranking_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonRankingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "final_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal finalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_tier", nullable = false, length = 20)
    private SeasonTier finalTier;

    @Column(name = "final_rank", nullable = false)
    private Integer finalRank;

    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    @Builder
    private SeasonRankingSnapshot(Season season, User user, BigDecimal finalScore, SeasonTier finalTier, int finalRank) {
        this.createdAt = LocalDateTime.now();
        this.season = season;
        this.user = user;
        this.finalScore = finalScore;
        this.finalTier = finalTier;
        this.finalRank = finalRank;
        this.snapshotAt = LocalDateTime.now();
    }
}
