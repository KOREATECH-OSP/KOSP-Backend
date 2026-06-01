package io.swkoreatech.kosp.common.title.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.title.model.enums.TitleCategory;
import io.swkoreatech.kosp.common.title.model.enums.TitleRarity;
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
 * 칭호 마스터 엔티티.
 *
 * <p>플랫폼에서 유저가 획득할 수 있는 칭호의 정의를 관리한다.
 * 새 칭호 추가는 DB INSERT 만으로 가능하며, 기존 코드 수정이 불필요하다.</p>
 */
@Getter
@Entity
@Table(name = "title")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Title extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TitleCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TitleRarity rarity;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private Title(
        String name,
        String code,
        String description,
        TitleCategory category,
        TitleRarity rarity,
        String iconUrl,
        boolean isActive,
        int displayOrder
    ) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.category = category;
        this.rarity = rarity;
        this.iconUrl = iconUrl;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
    }

    public void updateIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
