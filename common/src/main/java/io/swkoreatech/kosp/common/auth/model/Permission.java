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

    public void updateDescription(String description) {
        this.description = description;
    }
}
