package io.swkoreatech.kosp.domain.report.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
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
 * 신고 엔티티.
 * 게시글 또는 댓글에 대한 신고 정보를 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private LocalDateTime processedAt;

    @Builder
    private Report(User reporter, ReportTargetType targetType, Long targetId, ReportReason reason, String description) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.description = description;
        this.status = ReportStatus.PENDING;
    }

    /**
     * 신고를 처리한다.
     *
     * @param status 처리 상태
     */
    public void process(ReportStatus status) {
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}
