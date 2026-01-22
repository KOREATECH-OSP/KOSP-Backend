package io.swkoreatech.kosp.domain.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import io.swkoreatech.kosp.global.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Builder.Default
    @Column(name = "can_access_admin", columnDefinition = "BIT(1) DEFAULT 0", nullable = false)
    private Boolean canAccessAdmin = false;

    @ManyToMany
    @JoinTable(
        name = "role_policy",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "policy_id")
    )
    @Builder.Default
    private Set<Policy> policies = new HashSet<>();

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateCanAccessAdmin(Boolean canAccessAdmin) {
        if (canAccessAdmin != null) {
            this.canAccessAdmin = canAccessAdmin;
        }
    }
}
