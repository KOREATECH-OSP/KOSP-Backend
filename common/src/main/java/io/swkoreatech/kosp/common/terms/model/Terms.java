package io.swkoreatech.kosp.common.terms.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 문서 엔티티.
 *
 * <p>약관 종류({@link TermsType})별 버전 문서를 관리한다.
 * 버전은 종류 안에서만 유일하며, 활성 약관({@code isActive = true})은 종류마다 최대 1개다.
 * (DB: {@code uc_terms_type_version}, {@code uq_terms_active_per_type})</p>
 */
@Getter
@Entity
@Table(
    name = "terms",
    uniqueConstraints = @UniqueConstraint(name = "uc_terms_type_version", columnNames = {"type", "version"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TermsType type;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    private Terms(TermsType type, String version, String content, boolean isActive) {
        this.type = type;
        this.version = version;
        this.content = content;
        this.isActive = isActive;
    }
}
