package kr.ac.koreatech.sw.kosp.domain.admin.banner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.admin.banner.model.BannerSetting;

public interface BannerSettingRepository extends Repository<BannerSetting, Long> {

    Optional<BannerSetting> findById(Long id);

    BannerSetting save(BannerSetting bannerSetting);

    default BannerSetting getOrCreate() {
        return findById(1L).orElseGet(() -> save(BannerSetting.createDefault()));
    }
}
