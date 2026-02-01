package io.swkoreatech.kosp.domain.auth.model;

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
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "policy")
@Getter
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
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
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "policies")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePermissions(Set<Permission> newPermissions) {
        this.permissions.clear();
        this.permissions.addAll(newPermissions);
    }
}
