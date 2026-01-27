package io.swkoreatech.kosp.common.trigger.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "collection_trigger")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private TriggerPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TriggerStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public CollectionTrigger(Long userId, TriggerPriority priority, LocalDateTime scheduledAt) {
        this.userId = userId;
        this.priority = priority;
        this.status = TriggerStatus.PENDING;
        this.scheduledAt = scheduledAt != null ? scheduledAt : LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public static CollectionTrigger createImmediate(Long userId) {
        return CollectionTrigger.builder()
            .userId(userId)
            .priority(TriggerPriority.HIGH)
            .scheduledAt(LocalDateTime.now())
            .build();
    }

    public static CollectionTrigger createScheduled(Long userId, LocalDateTime scheduledAt) {
        return CollectionTrigger.builder()
            .userId(userId)
            .priority(TriggerPriority.LOW)
            .scheduledAt(scheduledAt)
            .build();
    }

    public void startProcessing() {
        this.status = TriggerStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = TriggerStatus.COMPLETED;
    }

    public void fail() {
        this.status = TriggerStatus.FAILED;
    }
}
