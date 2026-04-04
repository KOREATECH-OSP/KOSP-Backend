package io.swkoreatech.kosp.common.title.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.title.model.enums.TitleGrantSource;
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
 * 유저 보유 칭호 엔티티.
 *
 * <p>유저가 획득한 칭호를 기록한다.
 * 회수 시 {@link #isRevoked}를 true로 설정하는 소프트 회수 방식을 사용한다.
 * 대표 칭호({@link #isDisplay})는 유저당 최대 1개만 유지되도록 서비스 레이어에서 트랜잭션 처리한다.</p>
 */
@Getter
@Entity
@Table(name = "user_title")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTitle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "is_display", nullable = false)
    private boolean isDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_source", nullable = false, length = 20)
    private TitleGrantSource grantSource;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /** 회수한 관리자의 user_id (nullable) */
    @Column(name = "revoked_by")
    private Long revokedBy;

    @Builder
    private UserTitle(
        User user,
        Title title,
        boolean isDisplay,
        TitleGrantSource grantSource,
        LocalDateTime grantedAt
    ) {
        this.user = user;
        this.title = title;
        this.isDisplay = isDisplay;
        this.grantSource = grantSource;
        this.grantedAt = grantedAt;
        this.isRevoked = false;
    }

    /**
     * 대표 칭호로 설정한다.
     * 호출 전 동일 유저의 다른 user_title.is_display를 모두 false 처리해야 한다.
     */
    public void setAsDisplay() {
        this.isDisplay = true;
    }

    /**
     * 대표 칭호 해제한다.
     */
    public void unsetDisplay() {
        this.isDisplay = false;
    }

    /**
     * 칭호를 회수한다 (소프트 회수).
     *
     * @param revokedBy 회수한 관리자 user_id
     */
    public void revoke(Long revokedBy) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedBy = revokedBy;
        this.isDisplay = false;
    }
}
