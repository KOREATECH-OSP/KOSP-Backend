package io.swkoreatech.kosp.domain.resume.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이력서 ↔ 학습자료 자동 프로젝트 링크(예외 저장소).
 *
 * <p>자동 프로젝트는 {@link MaterialItem}(auto_imported=true)에서 실시간 투영으로 계산하며,
 * 이 엔티티는 그 투영에 대한 "예외"만 저장한다.</p>
 * <ul>
 *   <li>{@code deletedByUser}=true → 사용자가 삭제한 자동 프로젝트(재삽입 금지, tombstone)</li>
 *   <li>{@code userEdited}=true → 사용자 수정본. {@code overrides}(JSONB)로 원본 위에 덮어써 동기화로부터 보호한다.</li>
 *   <li>{@code visibility} → 프로젝트 단위 공개 override (기본 비공개)</li>
 * </ul>
 * <p>예외 행이 없는 자료는 자동으로 이력서에 포함된다.</p>
 */
@Getter
@Entity
@Table(name = "resume_material_project")
@NoArgsConstructor(access = PROTECTED)
public class ResumeMaterialProject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private UserResume resume;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_item_id", nullable = false)
    private MaterialItem materialItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private MaterialSource sourceType;

    @Column(name = "user_edited", nullable = false)
    private boolean userEdited;

    @Column(name = "deleted_by_user", nullable = false)
    private boolean deletedByUser;

    /** 사용자가 수정한 필드만 담은 JSON(부분 override). null 이면 원본 그대로 투영. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "overrides", columnDefinition = "jsonb")
    private String overrides;

    /** 프로젝트 단위 공개 override. 기본 PRIVATE. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 10)
    private Visibility visibility;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Builder
    private ResumeMaterialProject(
        UserResume resume,
        MaterialItem materialItem,
        MaterialSource sourceType,
        boolean userEdited,
        boolean deletedByUser,
        String overrides,
        Visibility visibility,
        LocalDateTime lastSyncedAt
    ) {
        this.resume = resume;
        this.materialItem = materialItem;
        this.sourceType = sourceType;
        this.userEdited = userEdited;
        this.deletedByUser = deletedByUser;
        this.overrides = overrides;
        this.visibility = visibility == null ? Visibility.PRIVATE : visibility;
        this.lastSyncedAt = lastSyncedAt;
    }

    /**
     * 자동 프로젝트를 삭제 처리한다(tombstone). 재동기화로 부활하지 않는다.
     */
    public void markDeleted() {
        this.deletedByUser = true;
    }

    /**
     * 삭제된 자동 프로젝트를 복원한다.
     */
    public void restore() {
        this.deletedByUser = false;
    }

    /**
     * 사용자 수정본을 반영하고 동기화 보호 플래그를 켠다.
     */
    public void applyOverrides(String overrides) {
        this.overrides = overrides;
        this.userEdited = true;
    }

    /**
     * 프로젝트 단위 공개 범위를 변경한다.
     */
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * 실제 적용되는 공개 여부.
     */
    public boolean isPublic() {
        return this.visibility == Visibility.PUBLIC;
    }
}
