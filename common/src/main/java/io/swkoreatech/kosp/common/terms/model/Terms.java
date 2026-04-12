package io.swkoreatech.kosp.common.terms.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 문서 엔티티.
 *
 * <p>서비스 이용약관의 버전별 문서를 관리한다.
 * 현재 유효한 약관은 {@code isActive = true} 인 레코드이며, 동시에 1개만 존재해야 한다.</p>
 */
@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false, unique = true, length = 20)
    private String version;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    private Terms(String version, String content, boolean isActive) {
        this.version = version;
        this.content = content;
        this.isActive = isActive;
    }
}
