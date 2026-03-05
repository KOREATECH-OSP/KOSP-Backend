package io.swkoreatech.kosp.common.auth.model;

import io.swkoreatech.kosp.common.model.BaseEntity;

import static lombok.AccessLevel.PROTECTED;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 권한(Permission) 엔티티.
 *
 * <p>시스템의 개별 권한을 나타내며, 여러 {@link Policy}에 포함될 수 있다.
 * RBAC(역할 기반 접근 제어) 모델에서 가장 세밀한 단위의 접근 제어 요소이다.</p>
 */
@Entity
@Table(name = "permission")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "permissions")
    private Set<Policy> policies = new HashSet<>();

    @Builder
    private Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 권한 설명을 수정한다.
     *
     * @param description 새로운 권한 설명
     */
    public void updateDescription(String description) {
        this.description = description;
    }
}
