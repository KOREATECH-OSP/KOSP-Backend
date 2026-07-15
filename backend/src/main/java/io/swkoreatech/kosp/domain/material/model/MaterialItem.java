package io.swkoreatech.kosp.domain.material.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.upload.model.Attachment;
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
 * 학습자료 아이템 엔티티.
 *
 * <p>과제/EL 자료 1건을 표현한다. 파일 실체는 {@code fileUrl} 등 메타데이터로 보관하며,
 * 기존 {@link Attachment}와의 연동이 필요하면 {@code attachment}로 참조한다.
 * 아이템 단위 {@code visibility}가 null이면 소속 폴더의 공개 설정을 상속한다.</p>
 */
@Getter
@Entity
@Table(name = "material_item")
@NoArgsConstructor(access = PROTECTED)
public class MaterialItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private MaterialFolder folder;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "material_year")
    private Integer materialYear;

    @Column(name = "semester", length = 20)
    private String semester;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private MaterialSource source;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    /** 아이템 단위 공개 override. null 이면 폴더 설정을 상속한다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 10)
    private Visibility visibility;

    @Column(name = "material_date")
    private LocalDateTime materialDate;

    @Builder
    private MaterialItem(
        MaterialFolder folder,
        User user,
        String title,
        String subjectName,
        Integer materialYear,
        String semester,
        MaterialSource source,
        String sourceUrl,
        Attachment attachment,
        String fileUrl,
        String originalFileName,
        Long fileSize,
        String contentType,
        Visibility visibility,
        LocalDateTime materialDate
    ) {
        this.folder = folder;
        this.user = user;
        this.title = title;
        this.subjectName = subjectName;
        this.materialYear = materialYear;
        this.semester = semester;
        this.source = source == null ? MaterialSource.MANUAL : source;
        this.sourceUrl = sourceUrl;
        this.attachment = attachment;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.visibility = visibility;
        this.materialDate = materialDate;
    }

    /**
     * 자료 메타데이터를 수정한다.
     */
    public void update(String title, String subjectName, Integer materialYear, String semester) {
        if (title != null) {
            this.title = title;
        }
        this.subjectName = subjectName;
        this.materialYear = materialYear;
        this.semester = semester;
    }

    /**
     * 소속 폴더를 이동한다.
     */
    public void moveTo(MaterialFolder folder) {
        this.folder = folder;
    }

    /**
     * 아이템 단위 공개 override 를 설정한다. null 이면 폴더 설정 상속.
     */
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * 실제 적용되는 공개 여부. 아이템 override 가 없으면 폴더 설정을 따른다.
     */
    public boolean isPublic() {
        if (this.visibility != null) {
            return this.visibility == Visibility.PUBLIC;
        }
        return this.folder != null && this.folder.isPublic();
    }
}
