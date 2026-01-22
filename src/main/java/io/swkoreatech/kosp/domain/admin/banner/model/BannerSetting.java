package io.swkoreatech.kosp.domain.admin.banner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "banner_setting")
public class BannerSetting {

    private static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false)
    private Boolean isActive;

    private BannerSetting(Boolean isActive) {
        this.id = SINGLETON_ID;
        this.isActive = isActive;
    }

    public static BannerSetting createDefault() {
        return new BannerSetting(false);
    }

    public boolean toggle() {
        this.isActive = !this.isActive;
        return this.isActive;
    }
}
