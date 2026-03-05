package io.swkoreatech.kosp.domain.admin.banner.repository;

import io.swkoreatech.kosp.domain.admin.banner.model.BannerSetting;

import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 배너 설정 저장소.
 * <p>배너 설정의 조회 및 저장 기능을 제공한다.</p>
 */
public interface BannerSettingRepository extends Repository<BannerSetting, Long> {

    /**
     * 식별자로 배너 설정을 조회한다.
     *
     * @param id 배너 설정 식별자
     * @return 배너 설정 Optional
     */
    Optional<BannerSetting> findById(Long id);

    /**
     * 배너 설정을 저장한다.
     *
     * @param bannerSetting 저장할 배너 설정
     * @return 저장된 배너 설정
     */
    BannerSetting save(BannerSetting bannerSetting);

    /**
     * 배너 설정을 조회하거나, 없으면 기본값을 생성하여 반환한다.
     *
     * @return 배너 설정 엔티티
     */
    default BannerSetting getOrCreate() {
        return findById(1L).orElseGet(() -> save(BannerSetting.createDefault()));
    }
}
