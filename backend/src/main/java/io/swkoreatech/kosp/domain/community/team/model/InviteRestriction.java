package io.swkoreatech.kosp.domain.community.team.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀 초대 반복 거절 제한 엔티티.
 *
 * <p>{@code (scope, scopeId, inviteeId)} 단위로 누적 거절 횟수를 관리한다.
 * 제한 정책은 <b>영구 3회 누적 + rolling 24시간 cooldown</b> 이다.</p>
 *
 * <ul>
 *   <li>거절 3회 미만: 제한 없음</li>
 *   <li>거절 3회 도달 이후: 매 거절마다 마지막 거절 시각 + 24시간까지 재초대 차단</li>
 *   <li>cooldown 종료 후에도 누적 횟수는 유지되므로, 다시 거절당하면 즉시 재차단된다</li>
 * </ul>
 *
 * <p>누적 횟수를 초기화하지 않는 이유는, 초기화 방식이면 24시간마다 3회씩
 * 영구히 초대를 반복할 수 있어 사실상 제한이 되지 않기 때문이다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "invite_restriction",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_invite_restriction",
            columnNames = {"scope", "scope_id", "invitee_id"}
        )
    }
)
public class InviteRestriction extends BaseEntity {

    /** 재초대를 차단하기 시작하는 누적 거절 횟수. */
    public static final int MAX_REJECTION = 3;

    /** 임계값 도달 후 재초대까지 대기해야 하는 시간(시). */
    public static final long COOLDOWN_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 10)
    private InviteRestrictionScope scope;

    /** scope 가 TEAM 이면 teamId, INVITER 이면 초대자 userId. */
    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @Column(name = "invitee_id", nullable = false)
    private Long inviteeId;

    @Column(name = "rejection_count", nullable = false)
    private int rejectionCount;

    @Column(name = "last_rejected_at")
    private LocalDateTime lastRejectedAt;

    /** 재초대가 가능해지는 시각. null 이면 제한 없음. */
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Builder
    private InviteRestriction(InviteRestrictionScope scope, Long scopeId, Long inviteeId) {
        this.scope = scope;
        this.scopeId = scopeId;
        this.inviteeId = inviteeId;
        this.rejectionCount = 0;
    }

    /**
     * 거절 1건을 반영한다.
     * 누적 횟수가 임계값 이상이면 지금부터 cooldown 만큼 재초대를 차단한다.
     *
     * @param rejectedAt 거절 시각
     */
    public void recordRejection(LocalDateTime rejectedAt) {
        this.rejectionCount++;
        this.lastRejectedAt = rejectedAt;
        if (this.rejectionCount >= MAX_REJECTION) {
            this.blockedUntil = rejectedAt.plusHours(COOLDOWN_HOURS);
        }
    }

    /**
     * 지정 시각 기준으로 재초대가 차단되어 있는지 여부.
     *
     * @param now 판정 기준 시각
     */
    public boolean isBlockedAt(LocalDateTime now) {
        return blockedUntil != null && now.isBefore(blockedUntil);
    }
}
