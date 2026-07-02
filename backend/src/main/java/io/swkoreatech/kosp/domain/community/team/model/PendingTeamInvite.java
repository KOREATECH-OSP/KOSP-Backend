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
 * 미가입자 팀 초대(대기) 엔티티.
 *
 * <p>아직 K-OSP에 가입하지 않은 코리아텍 이메일 사용자를 팀에 초대할 때 사용한다.
 * 초대받은 사람이 해당 이메일로 회원가입을 완료하면, 이 대기 초대가 실제
 * {@link TeamInvite}로 전환된다({@code PendingTeamInviteLinkListener}).</p>
 *
 * <p>{@code (team_id, email)} 유니크 제약으로 팀-이메일 조합당 1행만 유지하며,
 * 재초대 시 기존 행을 재사용({@link #reopen})한다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "pending_team_invite",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_pending_team_invite_team_email",
            columnNames = {"team_id", "email"}
        )
    }
)
public class PendingTeamInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PendingStatus status = PendingStatus.PENDING;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private PendingTeamInvite(Team team, User inviter, String email, Instant expiresAt) {
        this.team = team;
        this.inviter = inviter;
        this.email = email;
        this.expiresAt = expiresAt;
        this.status = PendingStatus.PENDING;
        this.isDeleted = false;
    }

    /** 초대 만료 여부. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 기존 대기 초대를 다시 발송한다(재초대). 발송자·만료 시각을 갱신하고 PENDING으로 되돌린다.
     */
    public void reopen(User inviter, Instant expiresAt) {
        this.inviter = inviter;
        this.expiresAt = expiresAt;
        this.status = PendingStatus.PENDING;
        this.isDeleted = false;
    }

    /** 회원가입 완료로 실제 초대로 전환되었음을 표시한다. */
    public void consume() {
        this.status = PendingStatus.CONSUMED;
        this.isDeleted = true;
    }

    /** 발신자 측에서 대기 초대를 취소한다. */
    public void cancel() {
        this.status = PendingStatus.CANCELLED;
        this.isDeleted = true;
    }

    /** 대기 초대 상태. */
    public enum PendingStatus {
        PENDING,    // 가입 대기 중
        CONSUMED,   // 가입 완료 → 실제 초대로 전환됨
        CANCELLED,  // 발신자 취소
        EXPIRED     // 만료
    }
}
