package io.swkoreatech.kosp.domain.community.recruit.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.domain.user.model.User;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruit_apply")
public class RecruitApply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    @Column(nullable = false)
    private String reason;

     @Column(name = "portfolio_url")
     private String portfolioUrl;

     @Column(name = "decision_reason", nullable = false, length = 500)
     private String decisionReason;

     public enum ApplyStatus {
        PENDING, ACCEPTED, REJECTED
    }

    @Builder
    private RecruitApply(Recruit recruit, User user, String reason, String portfolioUrl) {
        this.recruit = recruit;
        this.user = user;
        this.status = ApplyStatus.PENDING;
        this.reason = reason;
        this.portfolioUrl = portfolioUrl;
    }

     public void updateStatus(ApplyStatus status) {
         this.status = status;
     }

     public void updateDecisionReason(String reason) {
         if (reason == null || reason.isBlank()) {
             this.decisionReason = "사유 미입력";
             return;
         }
         this.decisionReason = reason;
     }
}
