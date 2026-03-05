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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 역할(Role) 엔티티.
 *
 * <p>사용자에게 부여되는 역할을 나타내며, 여러 {@link Policy}를 포함한다.
 * RBAC(역할 기반 접근 제어) 모델의 최상위 계층으로,
 * 관리자 페이지 접근 권한 여부를 결정하는 플래그를 포함한다.</p>
 */
@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "can_access_admin", nullable = false)
    private Boolean canAccessAdmin = false;

    @ManyToMany
    @JoinTable(
        name = "role_policy",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "policy_id")
    )
    private Set<Policy> policies = new HashSet<>();

    @Builder
    private Role(String name, String description, Boolean canAccessAdmin, Set<Policy> policies) {
        this.name = name;
        this.description = description;
        this.canAccessAdmin = canAccessAdmin != null && canAccessAdmin;
        this.policies = policies != null ? policies : new HashSet<>();
    }

    /**
     * 역할 설명을 수정한다.
     *
     * @param description 새로운 역할 설명
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 관리자 페이지 접근 권한 여부를 수정한다.
     *
     * @param canAccessAdmin 관리자 접근 가능 여부 ({@code null}이면 변경하지 않음)
     */
    public void updateCanAccessAdmin(Boolean canAccessAdmin) {
        if (canAccessAdmin != null) {
            this.canAccessAdmin = canAccessAdmin;
        }
    }
}
