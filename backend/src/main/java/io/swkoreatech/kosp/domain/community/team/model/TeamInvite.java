package io.swkoreatech.kosp.domain.community.team.model;

import java.time.Instant;

import io.swkoreatech.kosp.common.model.BaseEntity;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀 초대 엔티티.
 *
 * <p>초대 상태({@link InviteStatus})로 생명주기를 관리하며,
 * 거절 누적 횟수가 3회 이상이면 재발송을 차단한다.
 * 유효기간은 발송 시각 기준 7일이다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "team_invite",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_team_invite_team_invitee",
            columnNames = {"team_id", "invitee_id"}
        )
    }
)
public class TeamInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "rejection_count", nullable = false)
    private int rejectionCount = 0;

    @Builder
    private TeamInvite(Team team, User inviter, User invitee, Instant expiresAt) {
        this.team = team;
        this.inviter = inviter;
        this.invitee = invitee;
        this.expiresAt = expiresAt;
        this.status = InviteStatus.PENDING;
        this.rejectionCount = 0;
    }

    /** 초대 만료 여부를 확인한다. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** 초대를 수락한다. */
    public void accept() {
        this.status = InviteStatus.ACCEPTED;
        this.isDeleted = true;
    }

    /** 초대를 거절한다. 누적 거절 횟수를 증가시킨다. */
    public void reject() {
        this.status = InviteStatus.REJECTED;
        this.rejectionCount++;
        this.isDeleted = true;
    }

    /** 초대를 취소한다 (발신자 측). */
    public void cancel() {
        this.status = InviteStatus.CANCELLED;
        this.isDeleted = true;
    }

    /**
     * 종료된(취소/거절/만료/수락) 초대를 다시 PENDING 상태로 재발송한다.
     *
     * <p>{@code (team_id, invitee_id)} 유니크 제약 때문에 새 행을 INSERT하는 대신
     * 기존 행을 재사용한다.</p>
     *
     * @param inviter   재발송하는 사용자
     * @param expiresAt 새 만료 시각
     */
    public void reopen(User inviter, Instant expiresAt) {
        this.inviter = inviter;
        this.expiresAt = expiresAt;
        this.status = InviteStatus.PENDING;
        this.isDeleted = false;
    }

    /** 초대를 만료 처리한다. */
    public void expire() {
        this.status = InviteStatus.EXPIRED;
        this.isDeleted = true;
    }

    /** 논리 삭제 (하위 호환). */
    public void delete() {
        this.isDeleted = true;
    }

    /** 3회 이상 거절된 경우 재발송 차단 여부를 반환한다. */
    public boolean isBlocked() {
        return this.rejectionCount >= 3;
    }

    /**
     * 초대 상태.
     */
    public enum InviteStatus {
        PENDING,    // 대기 중
        ACCEPTED,   // 수락
        REJECTED,   // 거절
        CANCELLED,  // 취소 (발신자)
        EXPIRED     // 유효기간 만료
    }
}
