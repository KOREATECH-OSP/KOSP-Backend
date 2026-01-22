package io.swkoreatech.kosp.domain.challenge.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "current_progress")
    private Integer currentProgress;

    @Column(name = "target_progress")
    private Integer targetProgress;

    @Builder
    private ChallengeHistory(User user, Challenge challenge, boolean isAchieved, LocalDateTime achievedAt, Integer currentProgress, Integer targetProgress) {
        this.user = user;
        this.challenge = challenge;
        this.isAchieved = isAchieved;
        this.achievedAt = achievedAt;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
    }

    public void achieve() {
        this.isAchieved = true;
        this.achievedAt = LocalDateTime.now();
    }
}
