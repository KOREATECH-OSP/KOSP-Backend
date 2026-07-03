package io.swkoreatech.kosp.common.season.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
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
 * 시즌 랭킹 점수 집계 엔티티.
 *
 * <p>유저별 카테고리 점수와 총점, 티어, 순위를 관리한다.
 * 배치 실행 시 총점·티어·순위가 갱신된다.</p>
 */
@Getter
@Entity
@Table(name = "season_ranking_score")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonRankingScore extends BaseEntity {

    private static final BigDecimal MAX_TOTAL_SCORE = new BigDecimal("100.0000");
    private static final BigDecimal MAX_COMMIT_CHALLENGE_SCORE = new BigDecimal("35.0000");
    private static final BigDecimal MAX_PROJECT_SCORE = new BigDecimal("30.0000");
    private static final BigDecimal MAX_COMMUNITY_SCORE = new BigDecimal("10.0000");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal attendanceScore = BigDecimal.ZERO;

    @Column(name = "commit_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal commitScore = BigDecimal.ZERO;

    @Column(name = "challenge_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal challengeScore = BigDecimal.ZERO;

    @Column(name = "project_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal projectScore = BigDecimal.ZERO;

    @Column(name = "community_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal communityScore = BigDecimal.ZERO;

    @Column(name = "total_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeasonTier tier = SeasonTier.BRONZE_4;

    @Column(name = "rank_in_season")
    private Integer rankInSeason;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @Builder
    private SeasonRankingScore(Season season, User user) {
        this.season = season;
        this.user = user;
        this.attendanceScore = BigDecimal.ZERO;
        this.commitScore = BigDecimal.ZERO;
        this.challengeScore = BigDecimal.ZERO;
        this.projectScore = BigDecimal.ZERO;
        this.communityScore = BigDecimal.ZERO;
        this.totalScore = BigDecimal.ZERO;
        this.tier = SeasonTier.BRONZE_4;
    }

    public void addAttendanceScore(BigDecimal delta) {
        this.attendanceScore = this.attendanceScore.add(delta);
    }

    public void addChallengeScore(BigDecimal delta) {
        this.challengeScore = this.challengeScore.add(delta);
    }

    public void addProjectScore(BigDecimal delta) {
        this.projectScore = this.projectScore.add(delta).min(MAX_PROJECT_SCORE);
    }

    public void addCommunityScore(BigDecimal delta) {
        this.communityScore = this.communityScore.add(delta).min(MAX_COMMUNITY_SCORE);
    }

    public void updateCommitScore(BigDecimal newCommitScore) {
        this.commitScore = newCommitScore;
    }

    /**
     * 총점과 티어를 재계산한다. 커밋+챌린지 합산 35pt cap 적용.
     */
    public void recalculate() {
        BigDecimal commitChallenge = this.commitScore.add(this.challengeScore).min(MAX_COMMIT_CHALLENGE_SCORE);
        BigDecimal raw = this.attendanceScore
            .add(commitChallenge)
            .add(this.projectScore)
            .add(this.communityScore);
        this.totalScore = raw.min(MAX_TOTAL_SCORE).setScale(4, RoundingMode.HALF_UP);
        this.tier = SeasonTier.from(this.totalScore.doubleValue());
        this.lastCalculatedAt = LocalDateTime.now();
    }

    public void updateRank(int rank) {
        this.rankInSeason = rank;
    }

    /**
     * 티어를 직접 지정한다 (엘리트 승격용).
     *
     * <p>{@code recalculate()}가 점수형 티어를 세팅한 뒤, 배치의 엘리트 평가에서
     * Master/Challenger로 승격할 때 사용한다.</p>
     *
     * @param tier 지정할 티어
     */
    public void updateTier(SeasonTier tier) {
        this.tier = tier;
    }

    /**
     * 점수가 발생한(0보다 큰) 카테고리 수를 반환한다 (활동 다양성).
     *
     * @return 0보다 큰 점수를 가진 카테고리 수 (0~5)
     */
    public int activeCategoryCount() {
        int count = 0;
        if (attendanceScore.signum() > 0) count++;
        if (commitScore.signum() > 0) count++;
        if (challengeScore.signum() > 0) count++;
        if (projectScore.signum() > 0) count++;
        if (communityScore.signum() > 0) count++;
        return count;
    }
}
