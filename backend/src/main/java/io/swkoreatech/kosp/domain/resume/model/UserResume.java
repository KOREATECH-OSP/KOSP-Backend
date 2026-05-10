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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 이력서 엔티티.
 *
 * <p>한 사용자당 하나의 이력서를 가지며, 이력서 데이터는 JSONB 컬럼에 직렬화하여 저장한다.
 * 구조가 유연하고 필드가 많으므로 JSON 단일 컬럼 방식을 채택했다.</p>
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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 이력서 전체 데이터 (JSONB).
     * 프론트엔드 ResumeData 구조와 동일한 JSON 문자열을 저장한다.
     */
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_data", columnDefinition = "jsonb", nullable = false)
    private String resumeData;

    @Builder
    private UserResume(User user, String resumeData) {
        this.user = user;
        this.resumeData = resumeData;
    }

    /**
     * 이력서 데이터를 갱신한다.
     *
     * @param resumeData 새로운 이력서 JSON 문자열
     */
    public void updateResumeData(String resumeData) {
        this.resumeData = resumeData;
    }
}
