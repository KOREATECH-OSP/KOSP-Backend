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
 * 정책(Policy) 엔티티.
 *
 * <p>여러 {@link Permission}을 그룹화하는 정책 단위이다.
 * {@link Role}에 연결되어 역할 기반 접근 제어(RBAC) 모델의 중간 계층을 구성한다.</p>
 */
@Entity
@Table(name = "policy")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Policy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany
    @JoinTable(
        name = "policy_permission",
        joinColumns = @JoinColumn(name = "policy_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "policies")
    private Set<Role> roles = new HashSet<>();

    @Builder
    private Policy(String name, String description, Set<Permission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    /**
     * 정책 설명을 수정한다.
     *
     * @param description 새로운 정책 설명
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 정책에 포함된 권한 목록을 교체한다.
     *
     * <p>기존 권한을 모두 제거한 후, 새로운 권한 집합으로 대체한다.</p>
     *
     * @param newPermissions 새로운 권한 집합
     */
    public void updatePermissions(Set<Permission> newPermissions) {
        this.permissions.clear();
        this.permissions.addAll(newPermissions);
    }
}
