package io.swkoreatech.kosp.domain.admin.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import io.swkoreatech.kosp.domain.admin.banner.model.BannerSetting;
import io.swkoreatech.kosp.domain.admin.banner.repository.BannerSettingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerSettingRepository bannerSettingRepository;

    public BannerSettingResponse getSetting() {
        BannerSetting setting = bannerSettingRepository.getOrCreate();
        return new BannerSettingResponse(setting.getIsActive());
    }

    @Transactional
    public BannerSettingResponse toggle() {
        BannerSetting setting = bannerSettingRepository.getOrCreate();
        boolean newValue = setting.toggle();
        return new BannerSettingResponse(newValue);
    }
}
