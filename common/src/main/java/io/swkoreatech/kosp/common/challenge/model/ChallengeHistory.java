package io.swkoreatech.kosp.common.challenge.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 챌린지 이력 엔티티.
 *
 * <p>사용자별 챌린지 달성 이력을 관리한다.
 * 달성 여부, 달성 시각, 달성 시점의 진행도 등을 기록한다.</p>
 */
@Getter
@Entity
@Table(name = "challenge_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private boolean isAchieved;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    @Column(name = "progress_at_achievement")
    private Integer progressAtAchievement;

    @Builder
    private ChallengeHistory(
        User user,
        Challenge challenge,
        boolean isAchieved,
        LocalDateTime achievedAt,
        Integer progressAtAchievement
    ) {
        this.user = user;
        this.challenge = challenge;
        this.isAchieved = isAchieved;
        this.achievedAt = achievedAt;
        this.progressAtAchievement = progressAtAchievement;
    }

    /**
     * 챌린지를 달성 상태로 변경한다.
     *
     * <p>달성 플래그를 {@code true}로 설정하고, 달성 시각을 현재 시각으로 기록한다.</p>
     */
    public void achieve() {
        this.isAchieved = true;
        this.achievedAt = LocalDateTime.now();
    }

    /**
     * 챌린지 진행도를 갱신한다.
     *
     * @param progress 현재 진행 수치
     */
    public void updateProgress(int progress) {
        this.progressAtAchievement = progress;
    }
}
