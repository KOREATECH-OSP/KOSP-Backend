package io.swkoreatech.kosp.common.season.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectStatus;
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
 * 시즌 프로젝트 엔티티.
 *
 * <p>관리자가 프로젝트를 오픈하면 참여자를 등록할 수 있고,
 * 프로젝트를 종료(close)하면 참여자에게 점수가 일괄 지급된다.</p>
 */
@Getter
@Entity
@Table(name = "season_project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonProject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "project_level", nullable = false)
    private Integer projectLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeasonProjectStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(length = 500)
    private String note;

    @Builder
    private SeasonProject(Season season, String name, Integer projectLevel, Long createdBy, String note) {
        this.season = season;
        this.name = name;
        this.projectLevel = projectLevel;
        this.status = SeasonProjectStatus.OPEN;
        this.createdBy = createdBy;
        this.note = note;
    }

    /**
     * 프로젝트를 종료한다. 이후 참여자 점수가 일괄 지급된다.
     */
    public void close() {
        this.status = SeasonProjectStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.status == SeasonProjectStatus.OPEN;
    }
}
