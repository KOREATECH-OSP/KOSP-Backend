package io.swkoreatech.kosp.common.season.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectRole;
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
 * 시즌 프로젝트 참여자 엔티티.
 *
 * <p>프로젝트 종료 시 {@code scoreGranted}가 true로 변경되며 점수가 지급된다.</p>
 */
@Getter
@Entity
@Table(name = "season_project_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonProjectMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SeasonProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private SeasonProjectRole roleType;

    @Column(name = "score_granted", nullable = false)
    private boolean scoreGranted;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Builder
    private SeasonProjectMember(SeasonProject project, User user, SeasonProjectRole roleType) {
        this.project = project;
        this.user = user;
        this.roleType = roleType;
        this.scoreGranted = false;
    }

    public void markGranted() {
        this.scoreGranted = true;
        this.grantedAt = LocalDateTime.now();
    }

    public void changeRole(SeasonProjectRole newRole) {
        this.roleType = newRole;
    }
}
