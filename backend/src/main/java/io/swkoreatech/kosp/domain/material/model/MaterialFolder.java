package io.swkoreatech.kosp.domain.material.model;

import static lombok.AccessLevel.PROTECTED;

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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학습자료 폴더 엔티티.
 *
 * <p>연도 &gt; 학기 &gt; 과목 계층을 {@code parent} 자기참조로 표현하며,
 * 폴더 단위로 공개/비공개({@link Visibility})를 설정한다.
 * {@code isStartFolder}가 true인 폴더가 첨부파일 페이지 진입 시 기본 선택된다.</p>
 */
@Getter
@Entity
@Table(name = "material_folder")
@NoArgsConstructor(access = PROTECTED)
public class MaterialFolder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MaterialFolder parent;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false, length = 20)
    private FolderType folderType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private MaterialSource source;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 10)
    private Visibility visibility;

    @Column(name = "is_start_folder", nullable = false)
    private boolean isStartFolder;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private MaterialFolder(
        User user,
        MaterialFolder parent,
        String name,
        FolderType folderType,
        MaterialSource source,
        Visibility visibility,
        boolean isStartFolder,
        int sortOrder
    ) {
        this.user = user;
        this.parent = parent;
        this.name = name;
        this.folderType = folderType == null ? FolderType.CUSTOM : folderType;
        this.source = source == null ? MaterialSource.MANUAL : source;
        this.visibility = visibility == null ? Visibility.PRIVATE : visibility;
        this.isStartFolder = isStartFolder;
        this.sortOrder = sortOrder;
    }

    /**
     * 폴더 기본 정보를 수정한다.
     */
    public void update(String name, FolderType folderType, int sortOrder) {
        if (name != null) {
            this.name = name;
        }
        if (folderType != null) {
            this.folderType = folderType;
        }
        this.sortOrder = sortOrder;
    }

    /**
     * 폴더 공개 범위를 변경한다.
     */
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * 시작 폴더 여부를 설정한다.
     */
    public void setStartFolder(boolean isStartFolder) {
        this.isStartFolder = isStartFolder;
    }

    /**
     * 공개 폴더인지 여부를 반환한다.
     */
    public boolean isPublic() {
        return this.visibility == Visibility.PUBLIC;
    }
}
