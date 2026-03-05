package io.swkoreatech.kosp.domain.admin.banner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배너 설정 엔티티.
 * <p>싱글턴 패턴으로 구현되어 하나의 레코드만 유지한다.</p>
 */
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

    /**
     * 비활성 상태의 기본 배너 설정을 생성한다.
     *
     * @return 기본 배너 설정 엔티티
     */
    public static BannerSetting createDefault() {
        return new BannerSetting(false);
    }

    /**
     * 배너 활성화 상태를 토글한다.
     *
     * @return 토글 후 활성화 상태
     */
    public boolean toggle() {
        this.isActive = !this.isActive;
        return this.isActive;
    }
}
