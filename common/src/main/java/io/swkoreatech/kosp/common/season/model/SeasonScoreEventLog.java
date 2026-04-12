package io.swkoreatech.kosp.common.season.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
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
 * 시즌 점수 이벤트 로그 엔티티 (감사 원장, append-only).
 *
 * <p>모든 점수 지급 이벤트를 기록하며, 수정하지 않는다.
 * 보정이 필요한 경우 음수 delta 이벤트를 추가한다.</p>
 */
@Getter
@Entity
@Table(name = "season_score_event_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonScoreEventLog {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ScoreEventType eventType;

    @Column(name = "score_delta", nullable = false, precision = 8, scale = 4)
    private BigDecimal scoreDelta;

    @Column(name = "source_ref_id")
    private Long sourceRefId;

    @Column(name = "source_ref_type", length = 50)
    private String sourceRefType;

    @Column(length = 255)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Builder
    private SeasonScoreEventLog(
        Season season,
        User user,
        ScoreEventType eventType,
        BigDecimal scoreDelta,
        Long sourceRefId,
        String sourceRefType,
        String description,
        LocalDate eventDate
    ) {
        this.createdAt = LocalDateTime.now();
        this.season = season;
        this.user = user;
        this.eventType = eventType;
        this.scoreDelta = scoreDelta;
        this.sourceRefId = sourceRefId;
        this.sourceRefType = sourceRefType;
        this.description = description;
        this.eventDate = eventDate;
    }
}
