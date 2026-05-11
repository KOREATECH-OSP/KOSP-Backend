package io.swkoreatech.kosp.domain.resume.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 이력서 엔티티.
 *
 * <p>한 사용자가 여러 개의 이력서를 가질 수 있다.
 * 이력서 데이터는 JSONB 컬럼에 직렬화하여 저장한다.
 * {@code isDefault}가 true인 이력서가 기본 이력서로 사용된다.</p>
 */
@Getter
@Entity
@Table(name = "user_resume")
@NoArgsConstructor(access = PROTECTED)
public class UserResume extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 기본 이력서 여부.
     * 사용자당 최대 1개의 이력서가 기본 이력서로 설정된다.
     */
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    /**
     * 이력서 전체 데이터 (JSONB).
     * 프론트엔드 ResumeData 구조와 동일한 JSON 문자열을 저장한다.
     */
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_data", columnDefinition = "jsonb", nullable = false)
    private String resumeData;

    @Builder
    private UserResume(User user, String resumeData, boolean isDefault) {
        this.user = user;
        this.resumeData = resumeData;
        this.isDefault = isDefault;
    }

    /**
     * 이력서 데이터를 갱신한다.
     *
     * @param resumeData 새로운 이력서 JSON 문자열
     */
    public void updateResumeData(String resumeData) {
        this.resumeData = resumeData;
    }

    /**
     * 이 이력서를 기본 이력서로 설정한다.
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * 기본 이력서 설정을 해제한다.
     */
    public void unsetDefault() {
        this.isDefault = false;
    }
}
