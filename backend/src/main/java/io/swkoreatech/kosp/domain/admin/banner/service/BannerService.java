package io.swkoreatech.kosp.domain.admin.banner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import io.swkoreatech.kosp.domain.admin.banner.model.BannerSetting;
import io.swkoreatech.kosp.domain.admin.banner.repository.BannerSettingRepository;
import lombok.RequiredArgsConstructor;

/**
 * 배너 관리 서비스.
 * <p>배너 설정 조회 및 토글 기능을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerSettingRepository bannerSettingRepository;

    /**
     * 현재 배너 설정을 조회한다.
     *
     * @return 배너 설정 응답 DTO
     */
    public BannerSettingResponse getSetting() {
        BannerSetting setting = bannerSettingRepository.getOrCreate();
        return BannerSettingResponse.from(setting.getIsActive());
    }

    /**
     * 배너 활성화 상태를 토글한다.
     *
     * @return 토글 후 배너 설정 응답 DTO
     */
    @Transactional
    public BannerSettingResponse toggle() {
        BannerSetting setting = bannerSettingRepository.getOrCreate();
        return BannerSettingResponse.from(setting.toggle());
    }
}
