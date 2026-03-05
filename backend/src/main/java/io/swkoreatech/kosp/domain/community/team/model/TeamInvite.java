package io.swkoreatech.kosp.domain.community.team.model;

import java.time.Instant;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
/**
 * 팀 초대 엔티티.
 * 팀 초대 정보와 만료 여부를 관리한다.
 */
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

    @Builder
    private TeamInvite(Team team, User inviter, User invitee, Instant expiresAt) {
        this.team = team;
        this.inviter = inviter;
        this.invitee = invitee;
        this.expiresAt = expiresAt;
    }

    /**
     * 초대 만료 여부를 확인한다.
     *
     * @return 만료 여부
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** 초대를 논리 삭제한다. */
    public void delete() {
        this.isDeleted = true;
    }
}
