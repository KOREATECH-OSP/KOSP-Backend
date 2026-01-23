package io.swkoreatech.kosp.domain.challenge.model;

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

@Getter
@Entity
@Table(name = "challenge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "\"condition\"", nullable = false, columnDefinition = "TEXT")
    private String condition; // SpEL expression

    @Column(nullable = false)
    private Integer tier;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer point;

    @Builder
    private Challenge(String name, String description, String condition, Integer tier, String imageUrl, Integer point) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.tier = tier;
        this.imageUrl = imageUrl;
        this.point = point;
    }

    public void update(String name, String description, String condition, Integer tier, String imageUrl, Integer point) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.tier = tier;
        this.imageUrl = imageUrl;
        this.point = point;
    }
}
