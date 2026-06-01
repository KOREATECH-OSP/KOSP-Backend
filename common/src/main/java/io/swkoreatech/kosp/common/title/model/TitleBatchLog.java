package io.swkoreatech.kosp.common.title.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.title.model.enums.TitleBatchStatus;
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

/**
 * 칭호 배치 실행 로그 엔티티.
 *
 * <p>칭호 평가 배치가 실행될 때마다 로그를 기록한다.
 * 배치 시작 시 RUNNING 상태로 생성되고, 완료 또는 실패 시 업데이트된다.</p>
 */
@Getter
@Entity
@Table(name = "title_batch_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleBatchLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TitleBatchStatus status;

    @Column(name = "processed_user_count", nullable = false)
    private int processedUserCount;

    @Column(name = "granted_title_count", nullable = false)
    private int grantedTitleCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    private TitleBatchLog(LocalDateTime executedAt) {
        this.executedAt = executedAt;
        this.status = TitleBatchStatus.RUNNING;
        this.processedUserCount = 0;
        this.grantedTitleCount = 0;
    }

    public void complete(int processedUserCount, int grantedTitleCount) {
        this.status = TitleBatchStatus.SUCCESS;
        this.finishedAt = LocalDateTime.now();
        this.processedUserCount = processedUserCount;
        this.grantedTitleCount = grantedTitleCount;
    }

    public void fail(String errorMessage) {
        this.status = TitleBatchStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
