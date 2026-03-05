package io.swkoreatech.kosp.common.challenge.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 엔티티.
 *
 * <p>사용자가 달성할 수 있는 챌린지를 정의한다.
 * SpEL 표현식으로 작성된 달성 조건과 티어별 보상 포인트를 포함한다.</p>
 */
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

    @Column(name = "image_resource")
    private String imageResource;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_resource_type")
    private ImageResourceType imageResourceType;

    @Column(nullable = false)
    private Integer point;

    @Builder
    private Challenge(
        String name,
        String description,
        String condition,
        Integer tier,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer point
    ) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.tier = tier;
        this.imageResource = imageResource;
        this.imageResourceType = imageResourceType;
        this.point = point;
    }

    /**
     * 챌린지 정보를 수정한다.
     *
     * @param name              챌린지 이름
     * @param description       챌린지 설명
     * @param condition         달성 조건 (SpEL 표현식)
     * @param tier              챌린지 티어
     * @param imageResource     이미지 리소스 (아이콘 이름 또는 URL)
     * @param imageResourceType 이미지 리소스 유형
     * @param point             보상 포인트
     */
    public void update(
        String name,
        String description,
        String condition,
        Integer tier,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer point
    ) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.tier = tier;
        this.imageResource = imageResource;
        this.imageResourceType = imageResourceType;
        this.point = point;
    }
}
