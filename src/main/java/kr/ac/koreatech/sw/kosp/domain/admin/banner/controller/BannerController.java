package kr.ac.koreatech.sw.kosp.domain.admin.banner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.admin.banner.api.BannerApi;
import kr.ac.koreatech.sw.kosp.domain.admin.banner.dto.response.BannerSettingResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.banner.service.BannerService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BannerController implements BannerApi {

    private final BannerService bannerService;

    @Override
    @Permit(permitAll = true, name = "banner:read", description = "배너 설정 조회")
    public ResponseEntity<BannerSettingResponse> getSetting() {
        return ResponseEntity.ok(bannerService.getSetting());
    }

    @Override
    @Permit(name = "admin:banner:toggle", description = "배너 설정 토글")
    public ResponseEntity<BannerSettingResponse> toggle() {
        return ResponseEntity.ok(bannerService.toggle());
    }
}
