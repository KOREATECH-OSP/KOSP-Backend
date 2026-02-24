package io.swkoreatech.kosp.common.auth.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.HashSet;
import java.util.Set;

import io.swkoreatech.kosp.common.model.BaseEntity;
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

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateCanAccessAdmin(Boolean canAccessAdmin) {
        if (canAccessAdmin != null) {
            this.canAccessAdmin = canAccessAdmin;
        }
    }
}
