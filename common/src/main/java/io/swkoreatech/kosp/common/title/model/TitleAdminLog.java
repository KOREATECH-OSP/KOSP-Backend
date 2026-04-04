package io.swkoreatech.kosp.common.title.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.title.model.enums.TitleAdminAction;
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
 * 관리자 칭호 수동 조작 이력 엔티티.
 *
 * <p>관리자가 칭호를 수동 지급하거나 회수할 때마다 이력이 남는다.</p>
 */
@Getter
@Entity
@Table(name = "title_admin_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleAdminLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 조작한 관리자의 user_id */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** 대상 유저의 user_id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TitleAdminAction action;

    @Column(nullable = false, length = 500)
    private String reason;

    @Builder
    private TitleAdminLog(
        Long adminId,
        Long userId,
        Title title,
        TitleAdminAction action,
        String reason
    ) {
        this.adminId = adminId;
        this.userId = userId;
        this.title = title;
        this.action = action;
        this.reason = reason;
    }
}
