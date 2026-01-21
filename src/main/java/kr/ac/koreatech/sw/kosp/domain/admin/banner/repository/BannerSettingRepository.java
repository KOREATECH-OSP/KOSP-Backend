package kr.ac.koreatech.sw.kosp.domain.admin.banner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.admin.banner.model.BannerSetting;

public interface BannerSettingRepository extends JpaRepository<BannerSetting, Long> {

    default BannerSetting getOrCreate() {
        return findById(1L).orElseGet(() -> save(BannerSetting.createDefault()));
    }
}
